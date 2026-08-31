import { Component, OnInit, OnDestroy, AfterViewChecked, ViewChildren, QueryList, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import {
  Room,
  RoomEvent,
  RemoteParticipant,
  RemoteTrack,
  RemoteTrackPublication,
  LocalParticipant,
  Track
} from 'livekit-client';
import { AuthService, UserProfile } from '../../services/auth.service';
import { SalaService, Sala, SolicitacaoEntrada } from '../../services/sala.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

/** URL do servidor LiveKit — ajustar conforme o ambiente/deploy do backend. */
const LIVEKIT_URL = 'ws://localhost:7880';

export type ParticipantRole = 'viewer' | 'speaker' | 'moderator';

export interface ParticipantView {
  identity: string;
  name: string;
  role: ParticipantRole;
  muted: boolean;
  cameraOn: boolean;
  isLocal: boolean;
  wantsMic: boolean;
  wantsCam: boolean;
  participant: LocalParticipant | RemoteParticipant;
}

@Component({
  selector: 'app-audiencia-sala',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './audiencia-sala.component.html',
  styleUrls: ['./audiencia-sala.component.scss']
})
export class AudienciaSalaComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChildren('tileContainer') tileContainers!: QueryList<ElementRef<HTMLDivElement>>;

  salaId = 0;
  sala: Sala | null = null;
  isModerator = false;
  isLoading = true;
  loadError = '';

  room: Room | null = null;
  connectionState: 'idle' | 'requesting' | 'waiting' | 'connecting' | 'connected' | 'denied' | 'closed' = 'idle';

  participants: ParticipantView[] = [];
  pendingRequests: SolicitacaoEntrada[] = [];
  private userNames = new Map<number, string>();

  micOn = true;
  camOn = true;
  canPublishAudio = false;
  canPublishVideo = false;
  requestingPermission = false;

  private attachedIdentities = new Set<string>();
  private attachedAudioIdentities = new Set<string>();
  private audioElements = new Map<string, HTMLMediaElement>();
  private pollHandle: ReturnType<typeof setInterval> | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private salaService: SalaService
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    const role = this.authService.getUserRole();
    this.isModerator = role === 'MODERATOR' || role === 'ADMINISTRATOR';
    this.salaId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.salaId) {
      this.router.navigate(['/audiencia']);
      return;
    }
    this.loadSala();
  }

  ngOnDestroy(): void {
    this.room?.disconnect();
    this.cleanupAudioElements();
    if (this.pollHandle) clearInterval(this.pollHandle);
    this.pollHandle = null;
  }

  ngAfterViewChecked(): void {
    this.attachPendingTracks();
  }

  loadSala(): void {
    this.isLoading = true;
    this.loadError = '';
    this.salaService.getSalaById(this.salaId).subscribe({
      next: (sala) => {
        this.sala = sala;
        this.isLoading = false;
        if (this.isModerator) {
          this.connectToRoom();
          this.loadPendingRequests();
          this.pollHandle = setInterval(() => this.loadPendingRequests(), 3000);
        } else {
          this.requestEntry();
        }
      },
      error: () => {
        this.loadError = 'Sala não encontrada.';
        this.isLoading = false;
      }
    });
  }

  // ── Cidadão: solicitar entrada ──────────────────────────────

  requestEntry(): void {
    this.connectionState = 'requesting';
    // Se o usuário já foi aprovado em uma sessão anterior, o token já está
    // disponível e não é preciso (nem sempre é permitido) solicitar de novo.
    this.salaService.gerarToken(this.salaId).subscribe({
      next: ({ token }) => this.connectToRoom(token),
      error: () => this.createEntryRequest()
    });
  }

  private createEntryRequest(): void {
    this.salaService.solicitarEntrada(this.salaId).subscribe({
      next: () => {
        this.connectionState = 'waiting';
        this.tryJoin();
        this.pollHandle = setInterval(() => this.tryJoin(), 5000);
      },
      error: (err) => {
        console.error('Falha ao solicitar entrada na sala:', err);
        this.connectionState = 'denied';
      }
    });
  }

  private tryJoin(): void {
    if (this.connectionState === 'connected' || this.connectionState === 'connecting') return;
    this.salaService.gerarToken(this.salaId).subscribe({
      next: ({ token }) => {
        if (this.pollHandle) { clearInterval(this.pollHandle); this.pollHandle = null; }
        this.connectToRoom(token);
      },
      error: () => { /* ainda não aprovado, continua aguardando */ }
    });
  }

  // ── Conexão LiveKit ──────────────────────────────────────────

  private connectToRoom(existingToken?: string): void {
    this.connectionState = 'connecting';
    const token$ = existingToken
      ? of({ token: existingToken })
      : this.salaService.gerarToken(this.salaId);

    token$.pipe(catchError(err => { console.error('Falha ao gerar token LiveKit:', err); return of(null); })).subscribe(async (res) => {
      if (!res) { this.connectionState = 'denied'; return; }

      this.room?.disconnect();
      this.cleanupAudioElements();
      this.attachedIdentities.clear();
      const room = new Room();
      this.room = room;

      room.on(RoomEvent.ParticipantConnected, () => {
        this.syncParticipants();
        // Quando alguém entra, atualiza a fila de pendentes imediatamente
        if (this.isModerator) this.loadPendingRequests();
      });
      room.on(RoomEvent.ParticipantDisconnected, (p) => {
        this.audioElements.get(p.identity)?.remove();
        this.audioElements.delete(p.identity);
        this.attachedAudioIdentities.delete(p.identity);
        this.attachedIdentities.delete(p.identity);
        this.syncParticipants();
      });
      room.on(RoomEvent.TrackSubscribed, () => this.syncParticipants());
      room.on(RoomEvent.TrackUnsubscribed, (_track, pub, participant) => {
        if (pub.source === Track.Source.Camera) {
          this.removeVideoFromTile(participant.identity);
        }
        this.syncParticipants();
      });
      room.on(RoomEvent.TrackMuted, (pub, participant) => {
        if (pub.source === Track.Source.Camera) {
          this.removeVideoFromTile(participant.identity);
        }
        this.syncParticipants();
      });
      room.on(RoomEvent.TrackUnmuted, () => this.syncParticipants());      room.on(RoomEvent.ParticipantAttributesChanged, () => this.syncParticipants());
      // Dispara quando o participante local publica a câmera — necessário para
      // o attachPendingTracks retentar o vídeo local.
      room.on(RoomEvent.LocalTrackPublished, (pub) => {
        if (pub.source === Track.Source.Camera) {
          this.attachedIdentities.delete(room.localParticipant.identity);
        }
        this.syncParticipants();
      });
      room.on(RoomEvent.LocalTrackUnpublished, (pub) => {
        if (pub.source === Track.Source.Camera) {
          this.removeVideoFromTile(room.localParticipant.identity);
        }
        this.syncParticipants();
      });
      room.on(RoomEvent.ParticipantPermissionsChanged, (_prev, participant) => {
        if (participant.identity === room.localParticipant.identity) {
          this.retryPublishAfterPermissionChange();
        }
      });
      room.on(RoomEvent.Disconnected, () => {
        if (this.connectionState !== 'denied') {
          this.connectionState = 'idle';
        }
      });

      try {
        await room.connect(LIVEKIT_URL, res.token);
        this.connectionState = 'connected';
        this.syncParticipants();
      } catch (err) {
        console.error('Falha ao conectar ao LiveKit:', err);
        this.connectionState = 'denied';
        return;
      }

      if (this.isModerator) {
        // Moderador sempre tem permissão total, conforme o token gerado.
        this.canPublishAudio = true;
        this.canPublishVideo = true;
        try {
          await room.localParticipant.setMicrophoneEnabled(this.micOn);
          await room.localParticipant.setCameraEnabled(this.camOn);
        } catch (err) {
          console.error('Falha ao publicar áudio/vídeo do moderador:', err);
        }
        return;
      }

      // Cidadão: publicar áudio/vídeo é uma permissão separada da conexão,
      // só liberada depois que o moderador aprovar. Uma falha aqui não deve
      // derrubar quem já está conectado à sala.
      await this.tryEnableMic();
      await this.tryEnableCam();
    });
  }

  /** Tenta habilitar o microfone; atualiza canPublishAudio conforme o resultado real. */
  private async tryEnableMic(): Promise<void> {
    if (!this.room) return;
    try {
      await this.room.localParticipant.setMicrophoneEnabled(true);
      this.micOn = true;
      this.canPublishAudio = true;
    } catch {
      this.canPublishAudio = false;
    }
  }

  /** Tenta habilitar a câmera; atualiza canPublishVideo conforme o resultado real. */
  private async tryEnableCam(): Promise<void> {
    if (!this.room) return;
    try {
      await this.room.localParticipant.setCameraEnabled(true);
      this.camOn = true;
      this.canPublishVideo = true;
    } catch {
      this.canPublishVideo = false;
    }
  }

  /** Reage à liberação de mic/câmera pelo moderador em tempo real, tentando
   * publicar de novo e limpando o pedido de "quero falar" quando funcionar. */
  private async retryPublishAfterPermissionChange(): Promise<void> {
    const wasBlocked = !this.canPublishAudio || !this.canPublishVideo;
    await this.tryEnableMic();
    await this.tryEnableCam();
    if (wasBlocked && (this.canPublishAudio || this.canPublishVideo)) {
      this.requestingPermission = false;
      this.room?.localParticipant.setAttributes({ requestMic: '', requestCam: '' }).catch(() => {});
    }
  }

  /** Cidadão sinaliza ao moderador (via atributos do LiveKit, sem precisar de
   * endpoint de backend) que quer permissão para falar/mostrar a câmera. */
  async requestSpeakPermission(): Promise<void> {
    if (!this.room || this.requestingPermission) return;
    this.requestingPermission = true;
    try {
      await this.room.localParticipant.setAttributes({
        requestMic: (!this.canPublishAudio).toString(),
        requestCam: (!this.canPublishVideo).toString()
      });
    } catch (err) {
      console.error('Falha ao solicitar permissão para falar:', err);
    }
  }

  private syncParticipants(): void {
    if (!this.room) return;
    const local = this.room.localParticipant;
    const view: ParticipantView[] = [this.toView(local, true)];
    this.room.remoteParticipants.forEach(p => view.push(this.toView(p, false)));
    this.participants = view;
  }

  private toView(p: LocalParticipant | RemoteParticipant, isLocal: boolean): ParticipantView {
    const micPub = p.getTrackPublication(Track.Source.Microphone);
    const camPub = p.getTrackPublication(Track.Source.Camera);
    const role: ParticipantRole = this.sala && Number(p.identity) === this.sala.moderatorId ? 'moderator' : 'speaker';
    const attrs = p.attributes || {};
    return {
      identity: p.identity,
      name: p.name || p.identity,
      role,
      muted: !micPub || micPub.isMuted,
      cameraOn: !!camPub && !camPub.isMuted,
      isLocal,
      wantsMic: attrs['requestMic'] === 'true',
      wantsCam: attrs['requestCam'] === 'true',
      participant: p
    };
  }

  private attachPendingTracks(): void {
    this.attachRemoteAudio();
    if (!this.tileContainers) return;

    this.tileContainers.forEach(ref => {
      const identity = ref.nativeElement.getAttribute('data-identity');
      if (!identity) return;

      const view = this.participants.find(p => p.identity === identity);
      if (!view) return;

      // Só injeta se ainda não há um <video> no container
      const alreadyHasVideo = ref.nativeElement.querySelector('video') !== null;
      if (alreadyHasVideo) return;

      const camPub = view.participant.getTrackPublication(Track.Source.Camera);
      const track = camPub?.track;
      if (!track) return;

      const el = track.attach() as HTMLVideoElement;
      el.style.width = '100%';
      el.style.height = '100%';
      el.style.objectFit = 'cover';
      el.style.display = 'block';
      // Espelha câmera local
      if (view.isLocal) el.style.transform = 'scaleX(-1)';
      ref.nativeElement.appendChild(el);
    });
  }

  /** Remove o elemento <video> do tile de um participante (câmera desligada). */
  private removeVideoFromTile(identity: string): void {
    if (!this.tileContainers || !identity) return;
    this.tileContainers.forEach(ref => {
      if (ref.nativeElement.getAttribute('data-identity') === identity) {
        const video = ref.nativeElement.querySelector('video');
        video?.remove();
      }
    });
  }

  private attachRemoteAudio(): void {
    if (!this.room) return;
    this.room.remoteParticipants.forEach(p => {
      if (this.attachedAudioIdentities.has(p.identity)) return;
      const micPub = p.getTrackPublication(Track.Source.Microphone);
      const track = micPub?.track;
      if (!track) return;
      const el = track.attach() as HTMLAudioElement;
      el.autoplay = true;
      el.style.display = 'none';
      document.body.appendChild(el);
      this.audioElements.set(p.identity, el);
      this.attachedAudioIdentities.add(p.identity);
    });
  }

  private cleanupAudioElements(): void {
    this.audioElements.forEach(el => el.remove());
    this.audioElements.clear();
    this.attachedAudioIdentities.clear();
  }

  // ── Controles de mídia (usuário local) ──────────────────────

  async toggleMic(): Promise<void> {
    if (!this.room) return;
    this.micOn = !this.micOn;
    await this.room.localParticipant.setMicrophoneEnabled(this.micOn);
  }

  async toggleCam(): Promise<void> {
    if (!this.room) return;
    this.camOn = !this.camOn;
    await this.room.localParticipant.setCameraEnabled(this.camOn);
  }

  // ── Ações do moderador ───────────────────────────────────────

  loadPendingRequests(): void {
    this.salaService.getSolicitacoesEntrada(this.salaId).pipe(
      catchError(() => of([] as SolicitacaoEntrada[]))
    ).subscribe(reqs => {
      this.pendingRequests = reqs.filter(r => r.status === 'PENDING');
      this.loadRequestNames();
    });
  }

  private loadRequestNames(): void {
    const ids = [...new Set(this.pendingRequests.map(r => r.userId))];
    if (!ids.length) return;

    forkJoin(
      ids.map(id => this.authService.getUserById(id).pipe(catchError(() => of(null))))
    ).subscribe(users => {
      this.userNames.clear();
      users
        .filter((u): u is UserProfile => !!u)
        .forEach(u => {
          const name = u.fullname || u.name || `Usuário #${u.id}`;
          this.userNames.set(u.id, name);
        });
    });
  }

  getRequestDisplayName(req: SolicitacaoEntrada): string {
    return this.userNames.get(req.userId) || `Usuário #${req.userId}`;
  }

  approveRequest(req: SolicitacaoEntrada): void {
    this.salaService.aprovarSolicitacao(this.salaId, req.userId).subscribe({
      next: () => { this.pendingRequests = this.pendingRequests.filter(r => r.id !== req.id); },
      error: () => {}
    });
  }

  denyRequest(req: SolicitacaoEntrada): void {
    this.salaService.rejeitarSolicitacao(this.salaId, req.userId).subscribe({
      next: () => { this.pendingRequests = this.pendingRequests.filter(r => r.id !== req.id); },
      error: () => {}
    });
  }

  liberarMic(p: ParticipantView): void {
    this.salaService.liberarMicrofone(this.salaId, Number(p.identity)).subscribe({ error: () => {} });
  }

  bloquearMic(p: ParticipantView): void {
    this.salaService.bloquearMicrofone(this.salaId, Number(p.identity)).subscribe({ error: () => {} });
  }

  liberarCam(p: ParticipantView): void {
    this.salaService.liberarCamera(this.salaId, Number(p.identity)).subscribe({ error: () => {} });
  }

  bloquearCam(p: ParticipantView): void {
    this.salaService.bloquearCamera(this.salaId, Number(p.identity)).subscribe({ error: () => {} });
  }

  removeParticipant(p: ParticipantView): void {
    this.salaService.expulsarParticipante(this.salaId, Number(p.identity)).subscribe({
      next: () => { this.participants = this.participants.filter(x => x.identity !== p.identity); },
      error: () => {}
    });
  }

  encerrarSala(): void {
    if (!confirm('Encerrar esta sala para todos os participantes?')) return;
    this.salaService.encerrarSala(this.salaId).subscribe({
      next: () => {
        this.room?.disconnect();
        this.router.navigate(['/audiencia']);
      },
      error: () => {}
    });
  }

  // ── Helpers ────────────────────────────────────────────────

  getInitial(name: string): string {
    return name.charAt(0).toUpperCase();
  }

  /** Participantes que pediram mic ou câmera (usados no template) */
  get permissionRequesters(): ParticipantView[] {
    return this.participants.filter(p => !p.isLocal && (p.wantsMic || p.wantsCam));
  }

  /** True se ao menos um participante não-moderador pediu permissão */
  get hasPermissionRequests(): boolean {
    return this.permissionRequesters.length > 0;
  }

  /** True se o moderador já entrou na sala */
  get hasModerator(): boolean {
    return this.participants.some(p => p.role === 'moderator');
  }

  /** Cidadãos (não moderadores) */
  get citizenParticipants(): ParticipantView[] {
    return this.participants.filter(p => p.role !== 'moderator');
  }

  /** True se não há cidadãos na sala */
  get noCitizens(): boolean {
    return this.citizenParticipants.length === 0;
  }

  /** O participante que é moderador */
  get moderatorParticipant(): ParticipantView | undefined {
    return this.participants.find(p => p.role === 'moderator');
  }

  goBack(): void {
    this.room?.disconnect();
    this.router.navigate(['/audiencia']);
  }
}

