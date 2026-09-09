import { Component, OnInit, OnDestroy, NgZone, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import {
  Room,
  RoomEvent,
  LocalParticipant,
  Participant,
  RemoteTrack,
  RemoteTrackPublication,
  RemoteParticipant,
  Track
} from 'livekit-client';
import { AuthService } from '../../services/auth.service';
import { SalaService, Sala, SolicitacaoEntrada } from '../../services/sala.service';
import { ProjectService } from '../../services/project.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { LivekitVideoDirective } from './livekit-video.directive';

/** Estado da conexão do usuário atual com a sala. */
type ConnState =
  | 'loading'
  | 'requesting'   // cidadão obtendo token / pedindo entrada
  | 'waiting'      // cidadão aguardando aprovação do moderador
  | 'connecting'   // conectando ao LiveKit
  | 'connected'    // dentro da sala
  | 'denied'       // entrada negada / expulso
  | 'closed'       // sala encerrada
  | 'error';

/** View-model de um participante para o template. */
interface ParticipantVM {
  ref: Participant;
  identity: string;
  name: string;
  isLocal: boolean;
  isModerator: boolean;
  micEnabled: boolean;
  camEnabled: boolean;
  wantsMic: boolean;
  wantsCam: boolean;
}

@Component({
  selector: 'app-audiencia-sala',
  standalone: true,
  imports: [CommonModule, NavbarComponent, LivekitVideoDirective],
  templateUrl: './audiencia-sala.component.html',
  styleUrls: ['./audiencia-sala.component.scss']
})
export class AudienciaSalaComponent implements OnInit, OnDestroy {
  salaId = 0;
  sala: Sala | null = null;
  isModerator = false;
  myUserId = 0;

  connState: ConnState = 'loading';
  loadError = '';

  private room: Room | null = null;

  // Flags locais de mídia
  micOn = false;
  camOn = false;

  // Permissões concedidas pelo moderador (cidadão)
  canPublishAudio = false;
  canPublishVideo = false;

  // Cidadão já enviou pedido para falar
  requestingPermission = false;

  // Solicitações de entrada pendentes (moderador)
  pendingRequests: SolicitacaoEntrada[] = [];
  requestNames: Record<number, string> = {};

  private pendingPoll: any = null;
  private joinPoll: any = null;
  private falaPoll: any = null;
  private uiTick: any = null;

  // Áudio remoto precisa de gesto do usuário para tocar (autoplay policy).
  audioBlocked = false;

  // Solicitações de fala pendentes (moderador) — userId -> nome
  speakRequestIds: number[] = [];

  // Elementos <audio> criados para cada track de áudio remoto (identity -> el)
  private audioEls = new Map<string, HTMLAudioElement>();

  private readonly liveKitUrl = this.buildLiveKitUrl();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private salaService: SalaService,
    private projectService: ProjectService,
    private zone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  // ── Ciclo de vida ──────────────────────────────────────────

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    const role = this.authService.getUserRole();
    this.isModerator = role === 'MODERATOR' || role === 'ADMINISTRATOR';
    this.myUserId = this.authService.getUserId() ?? 0;
    this.salaId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadSala();
  }

  ngOnDestroy(): void {
    this.clearTimers();
    this.clearAllAudio();
    this.room?.disconnect();
    this.room = null;
  }

  private clearTimers(): void {
    if (this.pendingPoll) clearInterval(this.pendingPoll);
    if (this.joinPoll) clearInterval(this.joinPoll);
    if (this.falaPoll) clearInterval(this.falaPoll);
    if (this.uiTick) clearInterval(this.uiTick);
    this.pendingPoll = null;
    this.joinPoll = null;
    this.falaPoll = null;
    this.uiTick = null;
  }

  private buildLiveKitUrl(): string {
    // Mesmo host do backend, porta 7880 (espelha o app Flutter).
    try {
      const api = new URL('http://localhost:8080');
      const proto = api.protocol === 'https:' ? 'wss' : 'ws';
      return `${proto}://${api.hostname}:7880`;
    } catch {
      return 'ws://localhost:7880';
    }
  }

  // ── Carregar sala ──────────────────────────────────────────

  private loadSala(): void {
    this.connState = 'loading';
    this.salaService.getSalaById(this.salaId).subscribe({
      next: sala => {
        this.sala = sala;
        if (sala.status !== 'OPEN') {
          this.connState = 'closed';
          return;
        }
        if (this.isModerator) {
          this.connectToRoom();
          this.loadPendingRequests();
          this.loadSpeakRequests();
          this.pendingPoll = setInterval(() => this.loadPendingRequests(), 5000);
          this.falaPoll = setInterval(() => this.loadSpeakRequests(), 4000);
        } else {
          this.requestEntry();
        }
      },
      error: () => {
        this.loadError = 'Sala não encontrada.';
        this.connState = 'error';
      }
    });
  }

  // ── Fluxo cidadão ──────────────────────────────────────────

  private requestEntry(): void {
    this.connState = 'requesting';
    // Tenta obter token direto (caso já esteja aprovado, ex: reentrada).
    this.salaService.gerarToken(this.salaId).subscribe({
      next: res => this.connectToRoom(res.token),
      error: () => {
        // Ainda não aprovado — solicita entrada e passa a aguardar.
        this.salaService.solicitarEntrada(this.salaId).subscribe({
          next: () => {
            this.connState = 'waiting';
            this.startJoinPolling();
          },
          error: () => (this.connState = 'denied')
        });
      }
    });
  }

  private startJoinPolling(): void {
    if (this.joinPoll) clearInterval(this.joinPoll);
    this.joinPoll = setInterval(() => this.tryJoin(), 5000);
  }

  private tryJoin(): void {
    if (this.connState === 'connected' || this.connState === 'connecting') return;
    this.salaService.gerarToken(this.salaId).subscribe({
      next: res => {
        if (this.joinPoll) clearInterval(this.joinPoll);
        this.joinPoll = null;
        this.connectToRoom(res.token);
      },
      error: () => {
        /* continua aguardando */
      }
    });
  }

  // ── Conexão LiveKit ────────────────────────────────────────

  private async connectToRoom(token?: string): Promise<void> {
    this.connState = 'connecting';
    try {
      const authToken = token ?? (await this.gerarTokenAsync());
      const room = new Room({ adaptiveStream: true, dynacast: true });
      this.room = room;

      const refresh = () => this.zone.run(() => this.cdr.detectChanges());

      room
        .on(RoomEvent.ParticipantConnected, refresh)
        .on(RoomEvent.ParticipantDisconnected, (p: RemoteParticipant) => {
          this.removeAudioEl(p.identity);
          refresh();
        })
        .on(RoomEvent.TrackSubscribed, (track: RemoteTrack, _pub: RemoteTrackPublication, participant: RemoteParticipant) => {
          if (track.kind === Track.Kind.Audio) {
            this.attachRemoteAudio(track, participant.identity);
          }
          refresh();
        })
        .on(RoomEvent.TrackUnsubscribed, (track: RemoteTrack, _pub: RemoteTrackPublication, participant: RemoteParticipant) => {
          if (track.kind === Track.Kind.Audio) {
            this.removeAudioEl(participant.identity);
          }
          refresh();
        })
        .on(RoomEvent.TrackMuted, refresh)
        .on(RoomEvent.TrackUnmuted, refresh)
        .on(RoomEvent.LocalTrackPublished, refresh)
        .on(RoomEvent.LocalTrackUnpublished, refresh)
        .on(RoomEvent.ParticipantAttributesChanged, refresh)
        .on(RoomEvent.AudioPlaybackStatusChanged, () => {
          this.zone.run(() => {
            this.audioBlocked = !room.canPlaybackAudio;
            this.cdr.detectChanges();
          });
        })
        .on(RoomEvent.Disconnected, () => {
          this.zone.run(() => {
            // Sala encerrada ou expulso: se a sala segue OPEN e sou cidadão,
            // provavelmente fui expulso e preciso solicitar entrada de novo.
            this.connState = 'closed';
            this.cdr.detectChanges();
          });
        });

      await room.connect(this.liveKitUrl, authToken);

      // Tenta iniciar a reprodução de áudio; se o navegador bloquear,
      // exibimos um botão para o usuário liberar (política de autoplay).
      try {
        await room.startAudio();
      } catch {
        this.audioBlocked = true;
      }
      // Anexa áudios de participantes que já estavam publicando ao entrar.
      this.attachExistingAudio(room);

      this.zone.run(() => (this.connState = 'connected'));

      if (this.isModerator) {
        this.canPublishAudio = true;
        this.canPublishVideo = true;
        try {
          await room.localParticipant.setMicrophoneEnabled(true);
          await room.localParticipant.setCameraEnabled(true);
          this.micOn = true;
          this.camOn = true;
        } catch {
          /* dispositivos indisponíveis */
        }
      } else {
        await this.tryEnableMic();
        await this.tryEnableCam();
      }

      // Atualiza a UI periodicamente para refletir mudanças de permissão.
      this.uiTick = setInterval(() => this.zone.run(() => this.cdr.detectChanges()), 2000);
      refresh();
    } catch {
      this.zone.run(() => (this.connState = 'denied'));
    }
  }

  private gerarTokenAsync(): Promise<string> {
    return new Promise((resolve, reject) => {
      this.salaService.gerarToken(this.salaId).subscribe({
        next: res => resolve(res.token),
        error: err => reject(err)
      });
    });
  }

  private async tryEnableMic(): Promise<void> {
    try {
      await this.room?.localParticipant.setMicrophoneEnabled(true);
      this.micOn = true;
      this.canPublishAudio = true;
    } catch {
      this.canPublishAudio = false;
      this.micOn = false;
    }
  }

  private async tryEnableCam(): Promise<void> {
    try {
      await this.room?.localParticipant.setCameraEnabled(true);
      this.camOn = true;
      this.canPublishVideo = true;
    } catch {
      this.canPublishVideo = false;
      this.camOn = false;
    }
  }

  // ── Áudio remoto ───────────────────────────────────────────

  /** Cria/atualiza um <audio> oculto e anexa o track de áudio remoto. */
  private attachRemoteAudio(track: RemoteTrack, identity: string): void {
    this.removeAudioEl(identity);
    const el = track.attach() as HTMLAudioElement;
    el.autoplay = true;
    (el as any).playsInline = true;
    el.style.display = 'none';
    document.body.appendChild(el);
    this.audioEls.set(identity, el);
    el.play().catch(() => {
      this.audioBlocked = true;
    });
  }

  private removeAudioEl(identity: string): void {
    const el = this.audioEls.get(identity);
    if (el) {
      try {
        el.pause();
        el.srcObject = null;
        el.remove();
      } catch {
        /* ignore */
      }
      this.audioEls.delete(identity);
    }
  }

  /** Anexa áudio de participantes já conectados no momento da entrada. */
  private attachExistingAudio(room: Room): void {
    room.remoteParticipants.forEach(p => {
      p.audioTrackPublications.forEach(pub => {
        const track = pub.track;
        if (track && track.kind === Track.Kind.Audio) {
          this.attachRemoteAudio(track as RemoteTrack, p.identity);
        }
      });
    });
  }

  private clearAllAudio(): void {
    this.audioEls.forEach(el => {
      try {
        el.pause();
        el.srcObject = null;
        el.remove();
      } catch {
        /* ignore */
      }
    });
    this.audioEls.clear();
  }

  /** Botão exibido quando o navegador bloqueia autoplay de áudio. */
  async enableAudio(): Promise<void> {
    try {
      await this.room?.startAudio();
      this.audioEls.forEach(el => el.play().catch(() => {}));
      this.audioBlocked = false;
    } catch {
      /* ignore */
    }
    this.cdr.detectChanges();
  }

  // ── Controles de mídia (local) ─────────────────────────────

  async toggleMic(): Promise<void> {
    if (!this.canPublishAudio) return;
    this.micOn = !this.micOn;
    try {
      await this.room?.localParticipant.setMicrophoneEnabled(this.micOn);
    } catch {
      this.micOn = !this.micOn;
    }
    this.cdr.detectChanges();
  }

  async toggleCam(): Promise<void> {
    if (!this.canPublishVideo) return;
    this.camOn = !this.camOn;
    try {
      await this.room?.localParticipant.setCameraEnabled(this.camOn);
    } catch {
      this.camOn = !this.camOn;
    }
    this.cdr.detectChanges();
  }

  /** Cidadão pede para falar: registra no backend e sinaliza via atributos. */
  requestToSpeak(): void {
    if (this.requestingPermission) return;
    this.requestingPermission = true;

    // 1) Registra a solicitação de fala no backend (persistente).
    this.salaService.solicitarFala(this.salaId).subscribe({
      next: () => {},
      error: () => {}
    });

    // 2) Sinaliza em tempo real via atributos do LiveKit (redundância).
    const room = this.room;
    if (room) {
      room.localParticipant
        .setAttributes({
          requestMic: (!this.canPublishAudio).toString(),
          requestCam: (!this.canPublishVideo).toString()
        })
        .catch(() => {});
    }
    this.cdr.detectChanges();
  }

  /** Cidadão sai da sala (pode reentrar depois). */
  sairDaSala(): void {
    this.room?.disconnect();
    this.router.navigate(['/audiencia']);
  }

  // ── Ações do moderador — entrada ───────────────────────────

  private loadPendingRequests(): void {
    this.salaService.getSolicitacoesEntrada(this.salaId).subscribe({
      next: reqs => {
        const pending = reqs.filter(r => r.status === 'PENDING');
        this.pendingRequests = pending;
        pending.forEach(req => {
          if (this.requestNames[req.userId]) return;
          this.projectService.getUserById(req.userId).subscribe({
            next: u => (this.requestNames[req.userId] = u.fullname || u.name || `Usuário #${req.userId}`),
            error: () => (this.requestNames[req.userId] = `Usuário #${req.userId}`)
          });
        });
        this.cdr.detectChanges();
      },
      error: () => {
        /* ignore */
      }
    });
  }

  approveRequest(req: SolicitacaoEntrada): void {
    this.salaService.aprovarSolicitacao(this.salaId, req.userId).subscribe({
      next: () => {
        this.pendingRequests = this.pendingRequests.filter(r => r.id !== req.id);
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  denyRequest(req: SolicitacaoEntrada): void {
    this.salaService.rejeitarSolicitacao(this.salaId, req.userId).subscribe({
      next: () => {
        this.pendingRequests = this.pendingRequests.filter(r => r.id !== req.id);
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  // ── Ações do moderador — pedidos de fala (backend) ─────────

  private loadSpeakRequests(): void {
    this.salaService.getSolicitacoesFala(this.salaId).subscribe({
      next: reqs => {
        const pending = reqs.filter(r => r.status === 'PENDING');
        this.speakRequestIds = pending.map(r => r.userId);
        pending.forEach(req => {
          if (this.requestNames[req.userId]) return;
          this.projectService.getUserById(req.userId).subscribe({
            next: u => (this.requestNames[req.userId] = u.fullname || u.name || `Usuário #${req.userId}`),
            error: () => (this.requestNames[req.userId] = `Usuário #${req.userId}`)
          });
        });
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  /** Aprova o pedido de fala (libera microfone) do participante. */
  aprovarFala(userId: number): void {
    this.salaService.aprovarFala(this.salaId, userId).subscribe({
      next: () => {
        this.speakRequestIds = this.speakRequestIds.filter(id => id !== userId);
        this.cdr.detectChanges();
      },
      error: () => {
        // fallback: libera o microfone diretamente
        this.salaService.liberarMicrofone(this.salaId, userId).subscribe();
        this.speakRequestIds = this.speakRequestIds.filter(id => id !== userId);
        this.cdr.detectChanges();
      }
    });
  }

  rejeitarFala(userId: number): void {
    this.salaService.rejeitarFala(this.salaId, userId).subscribe({
      next: () => {
        this.speakRequestIds = this.speakRequestIds.filter(id => id !== userId);
        this.cdr.detectChanges();
      },
      error: () => {
        this.speakRequestIds = this.speakRequestIds.filter(id => id !== userId);
        this.cdr.detectChanges();
      }
    });
  }

  /** Pedidos de fala combinando backend (persistente) e atributos (tempo real). */
  get speakRequests(): { userId: number; name: string }[] {
    const map = new Map<number, string>();
    // Do backend
    for (const id of this.speakRequestIds) {
      map.set(id, this.requestNames[id] || `Usuário #${id}`);
    }
    // Dos atributos LiveKit (quem está conectado e sinalizou)
    for (const vm of this.citizenVMs) {
      if (!vm.isLocal && (vm.wantsMic || vm.wantsCam)) {
        const uid = Number(vm.identity);
        if (uid) map.set(uid, vm.name);
      }
    }
    return Array.from(map.entries()).map(([userId, name]) => ({ userId, name }));
  }

  // ── Ações do moderador — sobre participantes ───────────────

  liberarMic(p: ParticipantVM): void {
    const id = Number(p.identity);
    if (!id) return;
    this.salaService.liberarMicrofone(this.salaId, id).subscribe();
  }

  bloquearMic(p: ParticipantVM): void {
    const id = Number(p.identity);
    if (!id) return;
    this.salaService.bloquearMicrofone(this.salaId, id).subscribe();
  }

  liberarCam(p: ParticipantVM): void {
    const id = Number(p.identity);
    if (!id) return;
    this.salaService.liberarCamera(this.salaId, id).subscribe();
  }

  bloquearCam(p: ParticipantVM): void {
    const id = Number(p.identity);
    if (!id) return;
    this.salaService.bloquearCamera(this.salaId, id).subscribe();
  }

  /** Expulsa o cidadão — ele precisará solicitar entrada novamente. */
  expulsar(p: ParticipantVM): void {
    const id = Number(p.identity);
    if (!id) return;
    if (!confirm(`Remover ${p.name} da sala? Ele precisará solicitar entrada novamente.`)) return;
    this.salaService.expulsarParticipante(this.salaId, id).subscribe();
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

  // ── View-models de participantes ───────────────────────────

  private allParticipants(): Participant[] {
    const room = this.room;
    if (!room) return [];
    const list: Participant[] = [room.localParticipant, ...room.remoteParticipants.values()];
    return list;
  }

  private toVM(p: Participant): ParticipantVM {
    const isMod = this.sala != null && Number(p.identity) === this.sala.moderatorId;
    const micPubs = Array.from(p.audioTrackPublications.values());
    const camPubs = Array.from(p.videoTrackPublications.values());
    const micEnabled = micPubs.some(pub => !pub.isMuted && pub.track != null);
    const camEnabled = camPubs.some(pub => !pub.isMuted && pub.track != null);
    return {
      ref: p,
      identity: p.identity,
      name: p.name || p.identity,
      isLocal: p instanceof LocalParticipant,
      isModerator: isMod,
      micEnabled,
      camEnabled,
      wantsMic: p.attributes?.['requestMic'] === 'true',
      wantsCam: p.attributes?.['requestCam'] === 'true'
    };
  }

  get moderatorVM(): ParticipantVM | null {
    const mod = this.allParticipants().find(p => this.sala != null && Number(p.identity) === this.sala.moderatorId);
    return mod ? this.toVM(mod) : null;
  }

  get citizenVMs(): ParticipantVM[] {
    return this.allParticipants()
      .filter(p => !(this.sala != null && Number(p.identity) === this.sala.moderatorId))
      .map(p => this.toVM(p));
  }

  getInitial(name: string): string {
    return (name || '?').charAt(0).toUpperCase();
  }
}
