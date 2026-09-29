import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, LogEntry, LogPage, UserProfile } from '../../services/auth.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss']
})
export class LogsComponent implements OnInit {
  logs: LogEntry[] = [];
  users: UserProfile[] = [];
  private readonly actorNames = new Map<number, string>();
  private readonly resolvedActorIds = new Set<number>();
  page = 0;
  size = 20;
  readonly pageSizes = [20, 50, 100, 200];
  totalElements = 0;
  from = '';
  to = '';
  userId: number | null = null;
  method = '';
  readonly methods = ['POST', 'PUT', 'PATCH', 'DELETE'];
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn() || this.authService.getUserRole() !== 'ADMINISTRATOR') {
      this.router.navigate(['/projetos']);
      return;
    }
    this.loadUsers();
    this.loadActivities();
  }

  loadActivities(page = 0): void {
    if (this.from && this.to && this.from > this.to) {
      this.errorMessage = 'A data inicial não pode ser posterior à data final.';
      return;
    }
    this.page = page;
    this.isLoading = true;
    this.errorMessage = '';
    this.authService.getLogs({
      page,
      size: this.size,
      userId: this.userId ?? undefined,
      method: this.method || undefined,
      from: this.from || undefined,
      to: this.to || undefined
    }).subscribe({
      next: (result: LogPage) => {
        this.logs = result.content ?? [];
        this.page = result.page;
        this.totalElements = result.totalElements;
        this.isLoading = false;
        this.resolveActorNames(this.logs);
      },
      error: () => {
        this.errorMessage = 'Não foi possível carregar as atividades.';
        this.isLoading = false;
      }
    });
  }

  clearFilters(): void {
    this.from = '';
    this.to = '';
    this.userId = null;
    this.method = '';
    this.loadActivities();
  }

  changePageSize(size: number): void {
    this.size = size;
    this.loadActivities();
  }

  get totalPages(): number {
    return Math.ceil(this.totalElements / this.size);
  }

  get firstResult(): number {
    return this.totalElements ? this.page * this.size + 1 : 0;
  }

  get lastResult(): number {
    return Math.min((this.page + 1) * this.size, this.totalElements);
  }

  actorLabel(log: LogEntry): string {
    if (log.userId == null) return 'Visitante';
    return this.actorNames.get(log.userId) || 'Usuário';
  }

  userOptionLabel(user: UserProfile): string {
    return `${user.name || user.fullname || user.email} · #${user.id}`;
  }

  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime())
      ? value
      : new Intl.DateTimeFormat('pt-BR', {
          day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
        }).format(date).replace(',', ' às');
  }

  activityIcon(log: LogEntry): string {
    const path = (log.path ?? '').toLowerCase();
    if (path.includes('issue')) return 'report';
    if (path.includes('project')) return 'account_balance';
    if (path.includes('user')) return 'person';
    if (path.includes('settings') || path.includes('password')) return 'settings';
    return 'check_circle';
  }

  actionLabel(log: LogEntry): string {
    const path = (log.path ?? '').toLowerCase().replace(/\?.*$/, '');
    const method = (log.httpMethod ?? '').toUpperCase();
    const resource = path.includes('issue') ? 'uma ocorrência'
      : path.includes('project') ? 'um projeto'
      : path.includes('user') ? 'um usuário'
      : path.includes('subscription') ? 'as atualizações'
      : path.includes('settings') ? 'as configurações'
      : path.includes('category') ? 'uma categoria'
      : path.includes('municipality') ? 'um município'
      : path.includes('audience') || path.includes('meeting') ? 'uma audiência'
      : 'uma informação';

    if (path.includes('/authenticate')) return 'entrou no sistema';
    if (path.includes('forgot-password')) return 'pediu ajuda para recuperar a senha';
    if (path.includes('reset-password') || path.includes('update-password')) return 'atualizou a senha';
    if (path.includes('/subscriptions/')) {
      const verb = method === 'DELETE' ? 'deixou de receber' : 'passou a receber';
      if (path.includes('all-projects')) return `${verb} novidades de todos os projetos`;
      if (path.includes('all-issues')) return `${verb} novidades de todas as ocorrências`;
      return `${verb} atualizações`;
    }
    if (path.endsWith('/opinion')) return `deu sua opinião sobre ${resource}`;
    if (path.endsWith('/signature')) return method === 'DELETE' ? `retirou seu apoio a ${resource}` : `apoiou ${resource}`;
    if (path.includes('/councilor/') || path.endsWith('/associar')) return 'fez uma associação';
    if (path.endsWith('/approve')) return `aprovou ${resource}`;
    if (path.endsWith('/reject')) return `recusou ${resource}`;
    if (path.endsWith('/status') && method === 'PATCH') return `atualizou ${resource}`;
    if (path.includes('/image')) return method === 'DELETE' ? 'removeu uma imagem' : 'adicionou uma imagem';
    if (method === 'POST') return `cadastrou ${resource}`;
    if (method === 'PUT' || method === 'PATCH') return `atualizou ${resource}`;
    if (method === 'DELETE') return `removeu ${resource}`;
    return 'realizou uma atividade';
  }

  private loadUsers(): void {
    this.authService.getAllUsers().subscribe({
      next: users => {
        this.users = [...users].sort((first, second) =>
          (first.name || first.fullname || first.email).localeCompare(
            second.name || second.fullname || second.email,
            'pt-BR'
          )
        );
      },
      error: () => { this.users = []; }
    });
  }

  private resolveActorNames(logs: LogEntry[]): void {
    const ids = [...new Set(logs.map(log => log.userId).filter((id): id is number => id != null))];
    for (const id of ids) {
      if (this.resolvedActorIds.has(id)) continue;
      this.resolvedActorIds.add(id);
      this.authService.getUserById(id).subscribe({
        next: user => this.actorNames.set(id, user.name || ''),
        error: () => this.actorNames.set(id, '')
      });
    }
  }
}