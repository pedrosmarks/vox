import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

export type EventType =
  | 'status_change'
  | 'new_official'
  | 'new_suggestion'
  | 'voting_started'
  | 'completed';

export interface CommunityEvent {
  id: number;
  type: EventType;
  title: string;
  description: string;
  projectId?: number;
  projectTitle?: string;
  actor?: string;
  timestamp: Date;
}

export interface FollowableItem {
  id: string;
  name: string;
  subtitle?: string;
  type: 'project' | 'person';
}

// ── Mock data ─────────────────────────────────────────────
const MOCK_EVENTS: CommunityEvent[] = [
  {
    id: 1, type: 'status_change',
    title: 'Projeto aprovado para votação',
    description: 'O projeto "Construção de Ciclovia Central" saiu de Em Análise e agora está Em Votação.',
    projectId: 3, projectTitle: 'Construção de Ciclovia Central',
    timestamp: new Date('2026-05-31T08:30:00')
  },
  {
    id: 2, type: 'new_official',
    title: 'Novo projeto oficial publicado',
    description: 'O vereador João Alves publicou o projeto "Reforma do Parque Municipal".',
    projectId: 7, projectTitle: 'Reforma do Parque Municipal',
    actor: 'Vereador João Alves',
    timestamp: new Date('2026-05-30T15:10:00')
  },
  {
    id: 3, type: 'voting_started',
    title: 'Votação aberta',
    description: '"Ampliação do Hospital Municipal" entrou em votação. Sua voz importa!',
    projectId: 2, projectTitle: 'Ampliação do Hospital Municipal',
    timestamp: new Date('2026-05-30T09:00:00')
  },
  {
    id: 4, type: 'new_suggestion',
    title: 'Nova sugestão aprovada pela moderação',
    description: 'A sugestão "Iluminação LED no Bairro Jardim" foi aceita e está disponível para votação.',
    projectId: 11, projectTitle: 'Iluminação LED no Bairro Jardim',
    timestamp: new Date('2026-05-29T14:22:00')
  },
  {
    id: 5, type: 'completed',
    title: 'Projeto concluído',
    description: 'O projeto "Pavimentação da Rua das Flores" foi marcado como concluído.',
    projectId: 1, projectTitle: 'Pavimentação da Rua das Flores',
    timestamp: new Date('2026-05-28T11:45:00')
  },
  {
    id: 6, type: 'status_change',
    title: 'Projeto aprovado pelo conselho',
    description: '"Nova Praça do Trabalhador" foi aprovado pelo conselho e entra em execução em breve.',
    projectId: 5, projectTitle: 'Nova Praça do Trabalhador',
    timestamp: new Date('2026-05-27T16:00:00')
  },
  {
    id: 7, type: 'new_official',
    title: 'Novo projeto oficial publicado',
    description: 'A prefeitura publicou o projeto "Recapeamento Avenida Principal".',
    projectId: 9, projectTitle: 'Recapeamento Avenida Principal',
    actor: 'Prefeitura Municipal',
    timestamp: new Date('2026-05-26T10:30:00')
  }
];

// Mock de vereadores/autoridades — substituir por GET /api/user?role=MODERATOR quando disponível
const MOCK_PEOPLE: FollowableItem[] = [
  { id: 'p1', name: 'João Alves',     subtitle: 'Vereador',    type: 'person' },
  { id: 'p2', name: 'Maria Souza',    subtitle: 'Vereadora',   type: 'person' },
  { id: 'p3', name: 'Carlos Mendes',  subtitle: 'Vereador',    type: 'person' },
  { id: 'p4', name: 'Prefeitura',     subtitle: 'Oficial',     type: 'person' },
  { id: 'p5', name: 'Ana Paula Lima', subtitle: 'Vereadora',   type: 'person' },
];

// Mock de projetos em andamento — substituir por GET /api/project quando disponível
const MOCK_PROJECTS_FOLLOWABLE: FollowableItem[] = [
  { id: '2',  name: 'Ampliação do Hospital Municipal',    subtitle: 'Em votação',  type: 'project' },
  { id: '3',  name: 'Construção de Ciclovia Central',    subtitle: 'Em votação',  type: 'project' },
  { id: '5',  name: 'Nova Praça do Trabalhador',         subtitle: 'Aprovado',    type: 'project' },
  { id: '7',  name: 'Reforma do Parque Municipal',       subtitle: 'Publicado',   type: 'project' },
  { id: '9',  name: 'Recapeamento Avenida Principal',    subtitle: 'Publicado',   type: 'project' },
  { id: '11', name: 'Iluminação LED no Bairro Jardim',   subtitle: 'Em votação',  type: 'project' },
];

const STORAGE_KEY = 'vox_community_prefs';

interface CommunityPrefs {
  notifTypes: Record<EventType, boolean>;
  followedPeople: string[];
  followedProjects: string[];
}

const DEFAULT_PREFS: CommunityPrefs = {
  notifTypes: {
    voting_started: true,
    status_change:  true,
    new_official:   false,
    new_suggestion: false,
    completed:      true,
  },
  followedPeople:   [],
  followedProjects: [],
};

@Component({
  selector: 'app-comunidade',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, RouterModule],
  templateUrl: './comunidade.component.html',
  styleUrls: ['./comunidade.component.scss']
})
export class ComunidadeComponent implements OnInit {
  events: CommunityEvent[] = [];
  activeFilter: EventType | 'todos' = 'todos';

  // Seguindo
  prefs: CommunityPrefs = { ...DEFAULT_PREFS, notifTypes: { ...DEFAULT_PREFS.notifTypes } };
  followSearchPeople  = '';
  followSearchProject = '';
  showFollowPanel = false;

  peopleCatalog   = MOCK_PEOPLE;
  projectsCatalog = MOCK_PROJECTS_FOLLOWABLE;

  filters: { key: EventType | 'todos'; label: string }[] = [
    { key: 'todos',          label: 'Todos' },
    { key: 'voting_started', label: 'Votações abertas' },
    { key: 'status_change',  label: 'Mudanças de status' },
    { key: 'new_official',   label: 'Projetos oficiais' },
    { key: 'new_suggestion', label: 'Sugestões aprovadas' },
    { key: 'completed',      label: 'Concluídos' },
  ];

  notifTypeLabels: { key: EventType; label: string; icon: string }[] = [
    { key: 'voting_started', label: 'Votação aberta',        icon: '🗳️' },
    { key: 'status_change',  label: 'Mudanças de status',    icon: '🔄' },
    { key: 'new_official',   label: 'Novo projeto oficial',  icon: '🏛️' },
    { key: 'new_suggestion', label: 'Sugestão aprovada',     icon: '💡' },
    { key: 'completed',      label: 'Projeto concluído',     icon: '✅' },
  ];

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadPrefs();
    this.applyFilter();
  }

  // ── Prefs persistence (localStorage → backend quando disponível) ──────────
  private loadPrefs(): void {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) this.prefs = { ...DEFAULT_PREFS, ...JSON.parse(raw) };
    } catch { /* mantém default */ }
  }

  private savePrefs(): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this.prefs));
    // TODO: POST /api/user/preferences com this.prefs quando backend suportar
  }

  // ── Filtros de feed ───────────────────────────────────────────────────────
  setFilter(key: EventType | 'todos'): void {
    this.activeFilter = key;
    this.applyFilter();
  }

  applyFilter(): void {
    this.events = this.activeFilter === 'todos'
      ? [...MOCK_EVENTS]
      : MOCK_EVENTS.filter(e => e.type === this.activeFilter);
  }

  // ── Seguir / deixar de seguir ─────────────────────────────────────────────
  isFollowingPerson(id: string): boolean {
    return this.prefs.followedPeople.includes(id);
  }

  isFollowingProject(id: string): boolean {
    return this.prefs.followedProjects.includes(id);
  }

  togglePerson(id: string): void {
    this.prefs.followedPeople = this.isFollowingPerson(id)
      ? this.prefs.followedPeople.filter(x => x !== id)
      : [...this.prefs.followedPeople, id];
    this.savePrefs();
  }

  toggleProject(id: string): void {
    this.prefs.followedProjects = this.isFollowingProject(id)
      ? this.prefs.followedProjects.filter(x => x !== id)
      : [...this.prefs.followedProjects, id];
    this.savePrefs();
  }

  toggleNotifType(key: EventType): void {
    this.prefs.notifTypes[key] = !this.prefs.notifTypes[key];
    this.savePrefs();
  }

  get filteredPeople(): FollowableItem[] {
    const q = this.followSearchPeople.toLowerCase();
    return q ? this.peopleCatalog.filter(p => p.name.toLowerCase().includes(q)) : this.peopleCatalog;
  }

  get filteredProjects(): FollowableItem[] {
    const q = this.followSearchProject.toLowerCase();
    return q ? this.projectsCatalog.filter(p => p.name.toLowerCase().includes(q)) : this.projectsCatalog;
  }

  get followingCount(): number {
    return this.prefs.followedPeople.length + this.prefs.followedProjects.length;
  }

  // ── Helpers de display ────────────────────────────────────────────────────
  getIcon(type: EventType): string {
    const map: Record<EventType, string> = {
      status_change: '🔄', new_official: '🏛️',
      new_suggestion: '💡', voting_started: '🗳️', completed: '✅'
    };
    return map[type];
  }

  getTypeClass(type: EventType): string {
    const map: Record<EventType, string> = {
      status_change: 'event-status', new_official: 'event-official',
      new_suggestion: 'event-suggestion', voting_started: 'event-voting', completed: 'event-completed'
    };
    return map[type];
  }

  timeAgo(date: Date): string {
    const diff = Math.floor((Date.now() - date.getTime()) / 1000);
    if (diff < 60)    return 'agora mesmo';
    if (diff < 3600)  return `há ${Math.floor(diff / 60)} min`;
    if (diff < 86400) return `há ${Math.floor(diff / 3600)}h`;
    const days = Math.floor(diff / 86400);
    return days === 1 ? 'ontem' : `há ${days} dias`;
  }
}

