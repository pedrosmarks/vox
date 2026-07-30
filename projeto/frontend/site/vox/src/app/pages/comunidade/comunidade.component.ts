import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { catchError, of } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { NotificationService, Notification as AppNotification } from '../../services/notification.service';
import { ProjectService, Project } from '../../services/project.service';
import { SubscriptionService } from '../../services/subscription.service';
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
export class ComunidadeComponent implements OnInit, OnDestroy {
  events: CommunityEvent[] = [];
  activeFilter: EventType | 'todos' = 'todos';

  // Seguindo
  prefs: CommunityPrefs = { ...DEFAULT_PREFS, notifTypes: { ...DEFAULT_PREFS.notifTypes } };
  followSearchPeople  = '';
  followSearchProject = '';
  showFollowPanel = false;

  peopleCatalog:   FollowableItem[] = [];
  projectsCatalog: FollowableItem[] = [];

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

  private allEvents: CommunityEvent[] = [];
  private notificationEvents: CommunityEvent[] = [];
  private projectEvents: CommunityEvent[] = [];
  private seenNotificationIds = new Set<number>();
  private pollingHandle: ReturnType<typeof setInterval> | null = null;
  private readonly POLL_INTERVAL_MS = 30000;

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private projectService: ProjectService,
    private subscriptionService: SubscriptionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadPrefs();
    this.requestBrowserNotificationPermission();
    this.loadNotifications(true);
    this.loadCouncilors();
    this.loadFollowableProjects();
    this.loadProjectEvents();
    this.pollingHandle = setInterval(() => {
      this.loadNotifications(false);
      this.loadProjectEvents();
    }, this.POLL_INTERVAL_MS);
  }

  ngOnDestroy(): void {
    if (this.pollingHandle) {
      clearInterval(this.pollingHandle);
    }
  }

  private requestBrowserNotificationPermission(): void {
    if (typeof Notification !== 'undefined' && Notification.permission === 'default') {
      Notification.requestPermission();
    }
  }

  private loadNotifications(isInitialLoad: boolean): void {
    this.notificationService.getNotifications().pipe(
      catchError(() => of([] as AppNotification[]))
    ).subscribe(notifications => {
      if (!notifications.length) {
        this.notificationEvents = [];
        this.combineEvents();
        return;
      }

      if (!isInitialLoad) {
        const newOnes = notifications.filter(n => !this.seenNotificationIds.has(n.id));
        newOnes.forEach(n => this.showBrowserNotification(n));
      }

      notifications.forEach(n => this.seenNotificationIds.add(n.id));
      this.notificationEvents = notifications.map(n => this.mapNotificationToEvent(n));
      this.combineEvents();
    });
  }

  private showBrowserNotification(n: AppNotification): void {
    if (typeof Notification === 'undefined' || Notification.permission !== 'granted') return;
    new Notification('VOX Cidadão', { body: n.message });
  }

  private mapNotificationToEvent(n: AppNotification): CommunityEvent {
    return {
      id: n.id,
      type: 'status_change',
      title: n.message,
      description: n.message,
      timestamp: new Date(n.createdAt)
    };
  }

  // ── Sintetiza eventos a partir do estado atual dos projetos, já que o
  // backend ainda não gera notificações para publicação/mudança de status ──
  private loadProjectEvents(): void {
    this.projectService.getProjects().pipe(
      catchError(() => of([] as Project[]))
    ).subscribe(projects => {
      this.projectEvents = projects
        .map(p => this.mapProjectToEvent(p))
        .filter((e): e is CommunityEvent => e !== null);
      this.combineEvents();
    });
  }

  private mapProjectToEvent(p: Project): CommunityEvent | null {
    const isOfficial = p.isOfficial || p.type === 'OFFICIAL' || p.type === 'CHAMBER';
    const timestamp = new Date(p.updatedAt || p.createdAt);

    switch (p.status) {
      case 'IN_VOTING':
        return {
          id: 20000000 + p.id,
          type: 'voting_started',
          title: 'Votação aberta',
          description: `"${p.title}" entrou em votação. Sua voz importa!`,
          projectId: p.id, projectTitle: p.title,
          timestamp
        };
      case 'COMPLETED':
        return {
          id: 30000000 + p.id,
          type: 'completed',
          title: 'Projeto concluído',
          description: `O projeto "${p.title}" foi marcado como concluído.`,
          projectId: p.id, projectTitle: p.title,
          timestamp
        };
      case 'PUBLISHED':
        if (isOfficial) {
          return {
            id: 40000000 + p.id,
            type: 'new_official',
            title: 'Novo projeto oficial publicado',
            description: `Foi publicado o projeto "${p.title}".`,
            projectId: p.id, projectTitle: p.title,
            timestamp
          };
        }
        return {
          id: 50000000 + p.id,
          type: 'new_suggestion',
          title: 'Nova sugestão aprovada pela moderação',
          description: `A sugestão "${p.title}" foi aceita e está disponível para votação.`,
          projectId: p.id, projectTitle: p.title,
          timestamp
        };
      case 'SELECTED_BY_COUNCIL':
      case 'APPROVED_BY_COUNCIL':
      case 'IN_EXECUTION':
        return {
          id: 60000000 + p.id,
          type: 'status_change',
          title: 'Projeto mudou de status',
          description: `"${p.title}" agora está ${this.statusLabel(p.status)}.`,
          projectId: p.id, projectTitle: p.title,
          timestamp
        };
      default:
        return null;
    }
  }

  private statusLabel(status: string): string {
    const map: Record<string, string> = {
      SELECTED_BY_COUNCIL: 'selecionado pelo conselho',
      APPROVED_BY_COUNCIL: 'aprovado pelo conselho',
      IN_EXECUTION: 'em execução'
    };
    return map[status] ?? status;
  }

  private combineEvents(): void {
    this.allEvents = [...this.notificationEvents, ...this.projectEvents]
      .sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime());
    this.applyFilter();
  }

  private loadCouncilors(): void {
    this.authService.getCouncilors().pipe(
      catchError(() => of([]))
    ).subscribe(councilors => {
      this.peopleCatalog = councilors.map(c => ({ id: String(c.id), name: c.fullname || c.name, subtitle: 'Vereador(a)', type: 'person' as const }));
    });
  }

  private loadFollowableProjects(): void {
    this.projectService.getProjects().pipe(
      catchError(() => of([]))
    ).subscribe(projects => {
      this.projectsCatalog = projects.map(p => ({ id: String(p.id), name: p.title, subtitle: p.status, type: 'project' as const }));
    });
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
  }

  // ── Filtros de feed ───────────────────────────────────────────────────────
  setFilter(key: EventType | 'todos'): void {
    this.activeFilter = key;
    this.applyFilter();
  }

  applyFilter(): void {
    this.events = this.activeFilter === 'todos'
      ? [...this.allEvents]
      : this.allEvents.filter(e => e.type === this.activeFilter);
  }

  // ── Seguir / deixar de seguir ─────────────────────────────────────────────
  isFollowingPerson(id: string): boolean {
    return this.prefs.followedPeople.includes(id);
  }

  isFollowingProject(id: string): boolean {
    return this.prefs.followedProjects.includes(id);
  }

  togglePerson(id: string): void {
    const following = this.isFollowingPerson(id);
    this.prefs.followedPeople = following
      ? this.prefs.followedPeople.filter(x => x !== id)
      : [...this.prefs.followedPeople, id];
    this.savePrefs();

    const councilorId = Number(id);
    if (!Number.isFinite(councilorId)) return;
    const request$ = following
      ? this.subscriptionService.unsubscribeCouncilor(councilorId)
      : this.subscriptionService.subscribeCouncilor(councilorId);
    request$.pipe(catchError(() => of(undefined))).subscribe();
  }

  toggleProject(id: string): void {
    const following = this.isFollowingProject(id);
    this.prefs.followedProjects = following
      ? this.prefs.followedProjects.filter(x => x !== id)
      : [...this.prefs.followedProjects, id];
    this.savePrefs();

    const projectId = Number(id);
    if (!Number.isFinite(projectId)) return;
    const request$ = following
      ? this.subscriptionService.unsubscribeProject(projectId)
      : this.subscriptionService.subscribeProject(projectId);
    request$.pipe(catchError(() => of(undefined))).subscribe();
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

