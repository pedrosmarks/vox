import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

export interface Audiencia {
  id: number;
  titulo: string;
  descricao: string;
  dataInicio: Date;
  dataFim?: Date;
  status: 'agendada' | 'ao_vivo' | 'encerrada';
  participantes: number;
  criadoPor: string;
  projetoRelacionado?: string;
}

const MOCK_AUDIENCIAS: Audiencia[] = [
  {
    id: 1,
    titulo: 'Audiência Pública — Ciclovia Central',
    descricao: 'Discussão sobre o projeto de construção da ciclovia no centro da cidade. A população poderá se manifestar sobre o traçado proposto.',
    dataInicio: new Date(Date.now() + 2 * 60 * 60 * 1000), // daqui 2h
    status: 'ao_vivo',
    participantes: 47,
    criadoPor: 'Moderação VOX',
    projetoRelacionado: 'Construção de Ciclovia Central'
  },
  {
    id: 2,
    titulo: 'Audiência Pública — Ampliação do Hospital',
    descricao: 'Apresentação do projeto de ampliação do Hospital Municipal e coleta de sugestões da comunidade.',
    dataInicio: new Date(Date.now() + 24 * 60 * 60 * 1000), // amanhã
    status: 'agendada',
    participantes: 0,
    criadoPor: 'Moderação VOX',
    projetoRelacionado: 'Ampliação do Hospital Municipal'
  },
  {
    id: 3,
    titulo: 'Audiência Pública — Orçamento Municipal 2027',
    descricao: 'Participação popular na definição das prioridades do orçamento para o próximo exercício.',
    dataInicio: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000),
    status: 'agendada',
    participantes: 0,
    criadoPor: 'Prefeitura Municipal'
  },
  {
    id: 4,
    titulo: 'Audiência Pública — Plano Diretor',
    descricao: 'Revisão do Plano Diretor Municipal. Participação encerrada.',
    dataInicio: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
    dataFim: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000 + 2 * 60 * 60 * 1000),
    status: 'encerrada',
    participantes: 123,
    criadoPor: 'Prefeitura Municipal'
  }
];

@Component({
  selector: 'app-audiencia',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, RouterModule],
  templateUrl: './audiencia.component.html',
  styleUrls: ['./audiencia.component.scss']
})
export class AudienciaComponent implements OnInit {
  audiencias: Audiencia[] = MOCK_AUDIENCIAS;
  isModerator = false;
  showCreateForm = false;

  novaAudiencia = {
    titulo: '',
    descricao: '',
    dataInicio: '',
    horaInicio: '',
    projetoRelacionado: ''
  };

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    const role = this.authService.getUserRole();
    this.isModerator = role === 'MODERATOR' || role === 'ADMINISTRATOR';
  }

  entrar(id: number): void {
    this.router.navigate(['/audiencia', id]);
  }

  criarAudiencia(): void {
    if (!this.novaAudiencia.titulo.trim() || !this.novaAudiencia.dataInicio || !this.novaAudiencia.horaInicio) return;

    const dataHora = new Date(`${this.novaAudiencia.dataInicio}T${this.novaAudiencia.horaInicio}`);
    const nova: Audiencia = {
      id: this.audiencias.length + 1,
      titulo: this.novaAudiencia.titulo.trim(),
      descricao: this.novaAudiencia.descricao.trim(),
      dataInicio: dataHora,
      status: 'agendada',
      participantes: 0,
      criadoPor: 'Moderação VOX',
      projetoRelacionado: this.novaAudiencia.projetoRelacionado.trim() || undefined
    };

    // TODO: POST /api/audiencia quando backend suportar
    this.audiencias = [nova, ...this.audiencias];
    this.showCreateForm = false;
    this.novaAudiencia = { titulo: '', descricao: '', dataInicio: '', horaInicio: '', projetoRelacionado: '' };
  }

  getStatusLabel(status: string): string {
    return { ao_vivo: '🔴 Ao vivo', agendada: '📅 Agendada', encerrada: '⏹ Encerrada' }[status] ?? status;
  }

  formatDate(date: Date): string {
    return date.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
