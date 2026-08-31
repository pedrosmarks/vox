import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProjectService, Project, ProjectImage, UserSummary } from '../../services/project.service';
import { AuthService } from '../../services/auth.service';
import { SubscriptionService } from '../../services/subscription.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-projeto-detalhe',
  standalone: true,
  imports: [CommonModule, NavbarComponent, DatePipe, CurrencyPipe],
  templateUrl: './projeto-detalhe.component.html',
  styleUrls: ['./projeto-detalhe.component.scss']
})
export class ProjetoDetalheComponent implements OnInit {
  project: Project | null = null;
  images: ProjectImage[] = [];
  categoryName = '';
  authorName = '';
  isLoading = true;
  errorMessage = '';
  selectedImage = '';
  isModerator = false;
  isCouncilor = false;
  signed = false;
  isSigning = false;
  councilors: UserSummary[] = [];
  isCouncilorLinked = false;
  isLinkingCouncilor = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private authService: AuthService,
    private subscriptionService: SubscriptionService
  ) {}

  ngOnInit(): void {
    const role = this.authService.getUserRole();
    this.isModerator = role === 'MODERATOR' || role === 'ADMINISTRATOR';
    this.isCouncilor = role === 'COUNCILOR';
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/projetos']);
      return;
    }
    this.loadProject(id);
    this.loadSubscriptionState(id);
    if (this.isCouncilor || this.isModerator) {
      this.loadCouncilors(id);
    }
  }

  private loadSubscriptionState(projectId: number): void {
    this.subscriptionService.getSubscriptions().pipe(
      catchError(() => of([]))
    ).subscribe(subs => {
      this.signed = subs.some(s => s.type === 'PROJECT' && s.targetId === projectId);
    });
  }

  private loadCouncilors(projectId: number): void {
    this.projectService.getProjectCouncilors(projectId).pipe(
      catchError(() => of([] as UserSummary[]))
    ).subscribe(councilors => {
      this.councilors = councilors;
      const myId = this.authService.getUserId();
      this.isCouncilorLinked = !!myId && councilors.some(c => c.id === myId);
    });
  }

  loadProject(id: number): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.projectService.getProjectById(id).subscribe({
      next: (project) => {
        this.project = project;
        this.loadExtras(project);
      },
      error: () => {
        this.errorMessage = 'Projeto não encontrado ou erro ao carregar.';
        this.isLoading = false;
      }
    });
  }

  loadExtras(project: Project): void {
    const images$ = this.projectService.getProjectImages(project.id).pipe(catchError(() => of([])));
    const category$ = this.projectService.getCategoryById(project.categoryId).pipe(catchError(() => of(null)));
    const author$ = (project.authorId)
      ? this.projectService.getUserById(project.authorId).pipe(catchError(() => of(null)))
      : of(null);

    forkJoin([images$, category$, author$]).subscribe(([images, category, author]) => {
      this.images = images as ProjectImage[];
      this.selectedImage = this.images[0]?.url ?? '';
      this.categoryName = (category as any)?.name ?? '';
      this.authorName = (author as any)?.name ?? '';
      this.isLoading = false;
    });
  }

  selectImage(url: string): void {
    this.selectedImage = url;
  }

  goBack(): void {
    this.router.navigate(['/projetos']);
  }

  promoteProject(): void {
    if (this.project) {
      this.router.navigate(['/moderacao'], { queryParams: { promoteId: this.project.id } });
    }
  }

  toggleSign(): void {
    if (!this.project || this.isSigning) return;
    this.isSigning = true;
    const projectId = this.project.id;
    const action$ = this.signed
      ? this.subscriptionService.unsubscribeProject(projectId)
      : this.subscriptionService.subscribeProject(projectId);

    action$.subscribe({
      next: () => {
        this.signed = !this.signed;
        this.isSigning = false;
      },
      error: () => { this.isSigning = false; }
    });
  }

  toggleCouncilorLink(): void {
    if (!this.project || this.isLinkingCouncilor) return;
    const myId = this.authService.getUserId();
    if (!myId) return;
    this.isLinkingCouncilor = true;
    const projectId = this.project.id;
    const action$ = this.isCouncilorLinked
      ? this.projectService.unlinkCouncilor(projectId, myId)
      : this.projectService.linkCouncilor(projectId, myId);

    action$.subscribe({
      next: () => {
        this.isLinkingCouncilor = false;
        this.loadCouncilors(projectId);
      },
      error: () => { this.isLinkingCouncilor = false; }
    });
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING_APPROVAL: 'Em análise',
      IN_VOTING:        'Em votação',
      APPROVED:         'Aprovado',
      REJECTED:         'Rejeitado',
      IN_ANALYSIS:      'Em análise',
      COMPLETED:        'Concluído'
    };
    return map[status] ?? status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING_APPROVAL: 'status-analise',
      IN_VOTING:        'status-votacao',
      APPROVED:         'status-aprovado',
      REJECTED:         'status-rejeitado',
      IN_ANALYSIS:      'status-analise',
      COMPLETED:        'status-concluido'
    };
    return map[status] ?? 'status-analise';
  }

  getTypeLabel(project: Project): string {
    return project.isOfficial || project.type === 'OFFICIAL' ? 'Projeto Oficial' : 'Projeto Sugerido';
  }

  getTypeClass(project: Project): string {
    return project.isOfficial || project.type === 'OFFICIAL' ? 'type-oficial' : 'type-sugerido';
  }
}
