import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ProjectService, Project, OpinionStats } from '../../services/project.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { projectStatusLabel, statusClass } from '../../utils/status-labels';

type FilterKey = 'todos' | 'oficiais' | 'sugeridos' | 'apoiados';

@Component({
  selector: 'app-projetos',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './projetos.component.html',
  styleUrls: ['./projetos.component.scss']
})
export class ProjetosComponent implements OnInit {
  allProjects: Project[] = [];
  filteredProjects: Project[] = [];
  authorNames: Map<number, string> = new Map();
  activeFilter: FilterKey = 'todos';
  isLoading = true;
  errorMessage = '';
  isModerator = false;
  isCitizen = false;
  private signedProjects: Set<number> = new Set();
  private signingProjects: Set<number> = new Set();

  filters: { key: FilterKey; label: string }[] = [
    { key: 'todos',     label: 'Todos os projetos' },
    { key: 'oficiais',  label: 'Projetos oficiais' },
    { key: 'sugeridos', label: 'Projetos sugeridos' },
    { key: 'apoiados',  label: 'Mais apoiados' }
  ];

  /** Nº de apoios (opinion APPROVE) por projeto. */
  private approvals: Map<number, number> = new Map();
  /** Projetos que o cidadão atual apoiou. */
  private supportedProjects: Set<number> = new Set();
  /** Projetos com toggle de apoio em andamento. */
  private supportingProjects: Set<number> = new Set();

  constructor(
    private authService: AuthService,
    private projectService: ProjectService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    const role = this.authService.getUserRole();
    this.isModerator = role === 'MODERATOR' || role === 'ADMINISTRATOR';
    this.isCitizen = role === 'CITIZEN';
    this.loadProjects();
  }

  private isVisible(p: Project): boolean {
    // Projetos rejeitados, cancelados ou ainda em análise ficam fora do
    // catálogo público; aparecem apenas na fila de moderação.
    return p.status !== 'REJECTED' &&
      p.status !== 'CANCELLED' &&
      p.status !== 'PENDING_APPROVAL' &&
      p.status !== 'IN_ANALYSIS';
  }

  loadProjects(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.allProjects = projects.filter(p => this.isVisible(p));
        this.applyFilter();
        this.loadAuthorNames(this.allProjects);
        this.loadSignedStates(this.allProjects);
        this.loadApprovals(this.allProjects);
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar projetos. Tente novamente.';
        this.isLoading = false;
      }
    });
  }

  /** Carrega, para cada projeto, se o cidadão atual já assinou. */
  private loadSignedStates(projects: Project[]): void {
    if (!this.isCitizen) return; // botão de assinar só aparece p/ cidadão
    projects.forEach(p => {
      this.projectService
        .hasSignedProject(p.id)
        .pipe(catchError(() => of({ signed: false })))
        .subscribe(res => {
          if (res.signed) this.signedProjects.add(p.id);
          else this.signedProjects.delete(p.id);
        });
    });
  }

  /** Carrega a contagem de apoios de cada projeto e, para cidadão, se já apoiou. */
  private loadApprovals(projects: Project[]): void {
    projects.forEach(p => {
      this.projectService
        .getOpinionStats(p.id)
        .pipe(catchError(() => of({ approved: 0, disapproved: 0, neutral: 0, total: 0 } as OpinionStats)))
        .subscribe(stats => {
          this.approvals.set(p.id, stats.approved);
          // Se o filtro atual é "mais apoiados", reordena conforme os dados chegam.
          if (this.activeFilter === 'apoiados') this.applyFilter();
        });

      if (this.isCitizen) {
        this.projectService
          .getMyOpinion(p.id)
          .pipe(catchError(() => of(null)))
          .subscribe(op => {
            if (op?.opinion === 'APPROVE') this.supportedProjects.add(p.id);
            else this.supportedProjects.delete(p.id);
          });
      }
    });
  }

  loadAuthorNames(projects: Project[]): void {
    const citizenProjects = projects.filter(p => !p.isOfficial && p.type === 'CITIZEN');
    const uniqueIds = [...new Set(citizenProjects.map(p => p.authorId))];
    if (uniqueIds.length === 0) return;

    const requests = uniqueIds.map(id =>
      this.projectService.getUserById(id).pipe(catchError(() => of({ id, name: '', fullname: '' })))
    );

    forkJoin(requests).subscribe(users => {
      users.forEach(u => {
        const displayName = u.fullname || u.name;
        if (displayName) this.authorNames.set(u.id, displayName);
      });
    });
  }

  getAuthorName(authorId: number): string {
    return this.authorNames.get(authorId) ?? `Usuário #${authorId}`;
  }

  setFilter(key: FilterKey): void {
    this.activeFilter = key;
    this.applyFilter();
  }

  applyFilter(): void {
    switch (this.activeFilter) {
      case 'oficiais':
        this.filteredProjects = this.allProjects.filter(p => p.isOfficial || p.type === 'OFFICIAL');
        break;
      case 'sugeridos':
        this.filteredProjects = this.allProjects.filter(p => !p.isOfficial && p.type === 'CITIZEN');
        break;
      case 'apoiados':
        this.filteredProjects = [...this.allProjects].sort(
          (a, b) => this.getApprovals(b.id) - this.getApprovals(a.id)
        );
        break;
      default:
        this.filteredProjects = [...this.allProjects];
    }
  }

  /** Nº de apoios de um projeto (0 se ainda não carregado). */
  getApprovals(id: number): number {
    return this.approvals.get(id) ?? 0;
  }

  hasSupported(id: number): boolean {
    return this.supportedProjects.has(id);
  }

  isSupporting(id: number): boolean {
    return this.supportingProjects.has(id);
  }

  /** Apoia (APPROVE) ou remove o apoio (NEUTRAL) do projeto. */
  toggleSupport(id: number, event: MouseEvent): void {
    event.stopPropagation(); // não abrir o projeto ao clicar no botão
    if (this.supportingProjects.has(id)) return;

    const wasSupported = this.supportedProjects.has(id);
    this.supportingProjects.add(id);

    // Atualização otimista.
    if (wasSupported) {
      this.supportedProjects.delete(id);
      this.approvals.set(id, Math.max(0, this.getApprovals(id) - 1));
    } else {
      this.supportedProjects.add(id);
      this.approvals.set(id, this.getApprovals(id) + 1);
    }
    if (this.activeFilter === 'apoiados') this.applyFilter();

    const opinion = wasSupported ? 'NEUTRAL' : 'APPROVE';
    this.projectService.setOpinion(id, opinion).subscribe({
      next: () => this.supportingProjects.delete(id),
      error: () => {
        // Reverte em caso de falha.
        if (wasSupported) {
          this.supportedProjects.add(id);
          this.approvals.set(id, this.getApprovals(id) + 1);
        } else {
          this.supportedProjects.delete(id);
          this.approvals.set(id, Math.max(0, this.getApprovals(id) - 1));
        }
        this.supportingProjects.delete(id);
        if (this.activeFilter === 'apoiados') this.applyFilter();
      }
    });
  }

  getStatusLabel(status: string): string {
    return projectStatusLabel(status);
  }

  getStatusClass(status: string): string {
    return statusClass(status);
  }

  getTypeLabel(project: Project): string {
    return project.isOfficial || project.type === 'OFFICIAL' ? 'Projeto Oficial' : 'Projeto Sugerido';
  }

  getTypeClass(project: Project): string {
    return project.isOfficial || project.type === 'OFFICIAL' ? 'type-oficial' : 'type-sugerido';
  }

  openProject(id: number): void {
    this.router.navigate(['/projetos', id]);
  }

  promoteProject(id: number): void {
    this.router.navigate(['/moderacao'], { queryParams: { promoteId: id } });
  }

  toggleSign(id: number): void {
    if (this.signingProjects.has(id)) return; // evita duplo clique
    const wasSigned = this.signedProjects.has(id);
    this.signingProjects.add(id);

    // Atualização otimista da UI.
    if (wasSigned) this.signedProjects.delete(id);
    else this.signedProjects.add(id);

    const request$ = wasSigned
      ? this.projectService.unsignProject(id)
      : this.projectService.signProject(id);

    request$.subscribe({
      next: () => this.signingProjects.delete(id),
      error: () => {
        // Reverte em caso de falha.
        if (wasSigned) this.signedProjects.add(id);
        else this.signedProjects.delete(id);
        this.signingProjects.delete(id);
      }
    });
  }

  hasSigned(id: number): boolean {
    return this.signedProjects.has(id);
  }

  isSigning(id: number): boolean {
    return this.signingProjects.has(id);
  }
}
