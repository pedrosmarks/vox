import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { IssueService, IssueReport } from '../../services/issue.service';
import { AuthService } from '../../services/auth.service';
import { ProjectService, Category } from '../../services/project.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { issueStatusLabel, statusClass } from '../../utils/status-labels';

type TabKey = 'minhas' | 'disponiveis';

/** Tela do vereador para acompanhar e adotar ocorrências relatadas pelos cidadãos. */
@Component({
  selector: 'app-problemas',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './problemas.component.html',
  styleUrls: ['./problemas.component.scss']
})
export class ProblemasComponent implements OnInit {
  allIssues: IssueReport[] = [];
  isLoading = true;
  loadError = '';
  activeTab: TabKey = 'minhas';
  myId: number | null = null;
  linkingId: number | null = null;

  private categoryNames = new Map<number, string>();

  constructor(
    private issueService: IssueService,
    private authService: AuthService,
    private projectService: ProjectService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.myId = this.authService.getUserId();
    this.loadIssues();
    this.loadCategories();
  }

  private loadCategories(): void {
    this.projectService.getCategories().subscribe({
      next: cats => cats.forEach(c => this.categoryNames.set(c.id, c.name)),
      error: () => {}
    });
  }

  loadIssues(): void {
    this.isLoading = true;
    this.loadError = '';
    this.issueService.getIssues().subscribe({
      next: issues => {
        this.allIssues = issues;
        this.isLoading = false;
      },
      error: () => {
        this.loadError = 'Erro ao carregar problemas relatados.';
        this.isLoading = false;
      }
    });
  }

  get myIssues(): IssueReport[] {
    return this.allIssues.filter(i => this.myId != null && i.councilorId === this.myId);
  }

  get availableIssues(): IssueReport[] {
    // Disponíveis = sem vereador ou atribuídas a outro vereador.
    return this.allIssues.filter(i => i.councilorId == null || i.councilorId !== this.myId);
  }

  get visibleIssues(): IssueReport[] {
    return this.activeTab === 'minhas' ? this.myIssues : this.availableIssues;
  }

  setTab(tab: TabKey): void {
    this.activeTab = tab;
  }

  isMine(i: IssueReport): boolean {
    return this.myId != null && i.councilorId === this.myId;
  }

  categoryName(id: number): string {
    return this.categoryNames.get(id) ?? '—';
  }

  openIssue(i: IssueReport): void {
    this.router.navigate(['/problemas', i.id]);
  }

  /** Vereador adota (vincula a si) ou desvincula a ocorrência. */
  toggleLink(i: IssueReport, event: Event): void {
    event.stopPropagation();
    if (this.myId == null || this.linkingId === i.id) return;
    this.linkingId = i.id;
    const target = this.isMine(i) ? null : this.myId;
    this.issueService.assignCouncilor(i, target).subscribe({
      next: () => {
        i.councilorId = target;
        this.linkingId = null;
      },
      error: () => {
        this.loadError = 'Não foi possível atualizar o vínculo da ocorrência.';
        this.linkingId = null;
      }
    });
  }

  getStatusLabel(status: string): string {
    return issueStatusLabel(status);
  }

  getStatusClass(status: string): string {
    return statusClass(status);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('pt-BR');
  }
}
