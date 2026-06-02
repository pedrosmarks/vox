import { Component, OnInit, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

export type ParticipantRole = 'viewer' | 'speaker' | 'moderator';

export interface Participant {
  id: string;
  name: string;
  role: ParticipantRole;
  muted: boolean;
  cameraOn: boolean;
  isLocal?: boolean;
}

export interface SpeakerRequest {
  participantId: string;
  name: string;
  requestedAt: Date;
}

@Component({
  selector: 'app-audiencia-sala',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './audiencia-sala.component.html',
  styleUrls: ['./audiencia-sala.component.scss']
})
export class AudienciaSalaComponent implements OnInit, OnDestroy {
  @ViewChild('localVideo') localVideoRef!: ElementRef<HTMLVideoElement>;

  audienciaId = 0;
  isModerator = false;
  userName = '';

  // Estado do usuário
  mode: 'viewer' | 'speaker' | 'waiting' = 'viewer';
  localStream: MediaStream | null = null;
  micOn = true;
  camOn = true;
  handRaised = false;

  // Participantes mock
  // TODO: substituir por WebRTC + signaling (WebSocket /ws/audiencia/{id})
  participants: Participant[] = [
    { id: 'mod1', name: 'Moderação VOX', role: 'moderator', muted: false, cameraOn: true },
    { id: 'sp1',  name: 'João Silva',    role: 'speaker',   muted: false, cameraOn: true },
    { id: 'v1',   name: 'Maria Souza',   role: 'viewer',    muted: true,  cameraOn: false },
    { id: 'v2',   name: 'Carlos Lima',   role: 'viewer',    muted: true,  cameraOn: false },
  ];

  // Fila de pedidos de fala (visível apenas para moderador)
  speakerQueue: SpeakerRequest[] = [
    { participantId: 'v3', name: 'Ana Beatriz', requestedAt: new Date(Date.now() - 90000) },
    { participantId: 'v4', name: 'Roberto Alves', requestedAt: new Date(Date.now() - 30000) }
  ];

  audienciaTitle = 'Audiência Pública — Ciclovia Central';
  audienciaStatus: 'ao_vivo' | 'agendada' | 'encerrada' = 'ao_vivo';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    const role = this.authService.getUserRole();
    this.isModerator = role === 'MODERATOR' || role === 'ADMINISTRATOR';
    this.audienciaId = Number(this.route.snapshot.paramMap.get('id'));

    // Adiciona o próprio usuário como participante local
    const userId = String(this.authService.getUserId() ?? 'me');
    this.userName = 'Você';
    const localParticipant: Participant = {
      id: userId,
      name: this.userName,
      role: this.isModerator ? 'moderator' : 'viewer',
      muted: true,
      cameraOn: false,
      isLocal: true
    };
    this.participants = [localParticipant, ...this.participants];

    // Moderador entra diretamente como speaker
    if (this.isModerator) {
      this.mode = 'speaker';
      this.startLocalStream();
    }
  }

  ngOnDestroy(): void {
    this.stopLocalStream();
  }

  // ── Webcam ─────────────────────────────────────────────────
  async startLocalStream(): Promise<void> {
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
      // Aguarda o ViewChild estar disponível
      setTimeout(() => {
        if (this.localVideoRef?.nativeElement) {
          this.localVideoRef.nativeElement.srcObject = this.localStream;
        }
      }, 100);
      this.updateLocalParticipant({ cameraOn: true, muted: false });
    } catch {
      // Usuário negou permissão ou sem câmera — continua sem vídeo
      this.camOn = false;
      this.updateLocalParticipant({ cameraOn: false });
    }
  }

  stopLocalStream(): void {
    this.localStream?.getTracks().forEach(t => t.stop());
    this.localStream = null;
  }

  // ── Ações do cidadão ───────────────────────────────────────
  requestToSpeak(): void {
    this.handRaised = true;
    this.mode = 'waiting';
    // TODO: enviar via WebSocket { type: 'RAISE_HAND', participantId, name }
    const local = this.getLocalParticipant();
    if (local) {
      this.speakerQueue.push({
        participantId: local.id,
        name: local.name,
        requestedAt: new Date()
      });
    }
  }

  cancelRequest(): void {
    this.handRaised = false;
    this.mode = 'viewer';
    const local = this.getLocalParticipant();
    if (local) {
      this.speakerQueue = this.speakerQueue.filter(r => r.participantId !== local.id);
    }
  }

  toggleMic(): void {
    this.micOn = !this.micOn;
    this.localStream?.getAudioTracks().forEach(t => t.enabled = this.micOn);
    this.updateLocalParticipant({ muted: !this.micOn });
  }

  toggleCam(): void {
    this.camOn = !this.camOn;
    this.localStream?.getVideoTracks().forEach(t => t.enabled = this.camOn);
    this.updateLocalParticipant({ cameraOn: this.camOn });
  }

  encerrarFala(): void {
    this.mode = 'viewer';
    this.stopLocalStream();
    this.updateLocalParticipant({ role: 'viewer', cameraOn: false, muted: true });
  }

  // ── Ações do moderador ─────────────────────────────────────
  approveRequest(req: SpeakerRequest): void {
    this.speakerQueue = this.speakerQueue.filter(r => r.participantId !== req.participantId);
    const p = this.participants.find(p => p.id === req.participantId);
    if (p) p.role = 'speaker';
    else this.participants.push({ id: req.participantId, name: req.name, role: 'speaker', muted: false, cameraOn: true });
    // TODO: WebSocket { type: 'GRANT_SPEECH', participantId: req.participantId }
  }

  denyRequest(req: SpeakerRequest): void {
    this.speakerQueue = this.speakerQueue.filter(r => r.participantId !== req.participantId);
    // TODO: WebSocket { type: 'DENY_SPEECH', participantId: req.participantId }
  }

  muteParticipant(p: Participant): void {
    p.muted = !p.muted;
    // TODO: WebSocket { type: 'MUTE', participantId: p.id }
  }

  removeParticipant(p: Participant): void {
    this.participants = this.participants.filter(x => x.id !== p.id);
    this.speakerQueue = this.speakerQueue.filter(r => r.participantId !== p.id);
    // TODO: WebSocket { type: 'KICK', participantId: p.id }
  }

  encerrarAudiencia(): void {
    if (!confirm('Encerrar esta audiência para todos os participantes?')) return;
    this.audienciaStatus = 'encerrada';
    this.stopLocalStream();
    // TODO: POST /api/audiencia/{id}/encerrar
  }

  // ── Helpers ────────────────────────────────────────────────
  get speakers(): Participant[] {
    return this.participants.filter(p => p.role === 'speaker' || p.role === 'moderator');
  }

  get viewers(): Participant[] {
    return this.participants.filter(p => p.role === 'viewer');
  }

  private getLocalParticipant(): Participant | undefined {
    return this.participants.find(p => p.isLocal);
  }

  private updateLocalParticipant(changes: Partial<Participant>): void {
    const p = this.getLocalParticipant();
    if (p) Object.assign(p, changes);
  }

  getInitial(name: string): string {
    return name.charAt(0).toUpperCase();
  }

  timeAgo(date: Date): string {
    const diff = Math.floor((Date.now() - date.getTime()) / 1000);
    if (diff < 60)  return `há ${diff}s`;
    return `há ${Math.floor(diff / 60)}min`;
  }

  goBack(): void {
    this.stopLocalStream();
    this.router.navigate(['/audiencia']);
  }
}
