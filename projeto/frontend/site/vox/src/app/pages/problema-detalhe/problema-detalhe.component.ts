import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { IssueService, IssueReport, IssueImage } from '../../services/issue.service';
import { AuthService } from '../../services/auth.service';
import { ProjectService } from '../../services/project.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { issueStatusLabel, statusClass } from '../../utils/status-labels';

@Component({
  selector: 'app-problema-detalhe',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, DatePipe],
  templateUrl: './problema-detalhe.component.html',
  styleUrls: ['./problema-detalhe.component.scss']
})
export class ProblemaDetalheComponent implements OnInit {
  issue: IssueReport | null = null;
  images: IssueImage[] = [];
  selectedImage = '';
  categoryName = '';
  authorName = '';
  councilorName = '';

  isLoading = true;
  errorMessage = '';

  isModerator = false;
  isCouncilor = false;
  myId: number | null = null;

  linking = false;
  savingStatus = false;
  statusMessage = '';

  // Status possíveis de ocorrência (API.md).
  readonly statuses = [
    'OPEN',
    'UNDER_REVIEW',
    'IN_PROGRESS',
    'FORWARDED',
    'RESOLVED',
    'REJECTED',
    'CLOSED'
  ];
  selectedStatus = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private issueService: IssueService,
    private authService: AuthService,
    private projectService: ProjectService
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    const role = this.authService.getUserRole();
    this.isModerator = role === 'MODERATOR' || role === 'ADMINISTRATOR';
    this.isCouncilor = role === 'COUNCILOR';
    this.myId = this.authService.getUserId();

    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/problemas']);
      return;
    }
    this.load(id);
  }

  load(id: number): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.issueService.getIssueById(id).subscribe({
      next: issue => {
        this.issue = issue;
        this.selectedStatus = issue.status;
        this.loadExtras(issue);
      },
      error: () => {
        this.errorMessage = 'Ocorrência não encontrada ou erro ao carregar.';
        this.isLoading = false;
      }
    });
  }

  private loadExtras(issue: IssueReport): void {
    const images$ = this.issueService.getIssueImages(issue.id).pipe(catchError(() => of([])));
    const category$ = this.projectService.getCategoryById(issue.categoryId).pipe(catchError(() => of(null)));
    const author$ = issue.authorId
      ? this.projectService.getUserById(issue.authorId).pipe(catchError(() => of(null)))
      : of(null);
    const councilor$ = issue.councilorId
      ? this.authService.getCouncilorById(issue.councilorId).pipe(catchError(() => of(null)))
      : of(null);

    forkJoin([images$, category$, author$, councilor$]).subscribe(([images, category, author, councilor]) => {
      this.images = images as IssueImage[];
      this.selectedImage = this.images[0]?.url ?? '';
      this.categoryName = (category as any)?.name ?? '';
      this.authorName = (author as any)?.name ?? '';
      this.councilorName = (councilor as any)?.fullname || (councilor as any)?.name || '';
      this.isLoading = false;
    });
  }

  get isMine(): boolean {
    return this.myId != null && this.issue?.councilorId === this.myId;
  }

  selectImage(url: string): void {
    this.selectedImage = url;
  }

  goBack(): void {
    this.router.navigate(['/problemas']);
  }

  /** Vereador adota/desvincula a ocorrência. */
  toggleLink(): void {
    if (!this.issue || this.myId == null || this.linking) return;
    this.linking = true;
    const linking = !this.isMine;
    // Associar usa o endpoint dedicado; desassociar cai no update JSON.
    const request$ = linking
      ? this.issueService.associate(this.issue.id)
      : this.issueService.unassignCouncilor(this.issue);
    request$.subscribe({
      next: () => {
        if (this.issue) this.issue.councilorId = linking ? this.myId : null;
        this.councilorName = linking ? 'Você' : '';
        this.linking = false;
      },
      error: () => {
        this.errorMessage = 'Não foi possível atualizar o vínculo.';
        this.linking = false;
      }
    });
  }

  updateStatus(): void {
    if (!this.issue || this.savingStatus) return;
    if (this.selectedStatus === this.issue.status) return;
    this.savingStatus = true;
    this.statusMessage = '';
    this.issueService.updateIssueStatus(this.issue.id, this.selectedStatus).subscribe({
      next: () => {
        if (this.issue) this.issue.status = this.selectedStatus;
        this.savingStatus = false;
        this.statusMessage = 'Status atualizado.';
      },
      error: () => {
        this.savingStatus = false;
        this.statusMessage = 'Não foi possível atualizar o status.';
      }
    });
  }

  statusLabel(status: string): string {
    return issueStatusLabel(status);
  }

  statusClass(status: string): string {
    return statusClass(status);
  }
}
