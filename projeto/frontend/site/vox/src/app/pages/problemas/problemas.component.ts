import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { IssueService, IssueReport } from '../../services/issue.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

/** Tela do vereador para acompanhar problemas relatados pelos cidadãos do município. */
@Component({
  selector: 'app-problemas',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './problemas.component.html',
  styleUrls: ['./problemas.component.scss']
})
export class ProblemasComponent implements OnInit {
  issues: IssueReport[] = [];
  isLoading = true;
  loadError = '';

  constructor(
    private issueService: IssueService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadIssues();
  }

  loadIssues(): void {
    this.isLoading = true;
    this.loadError = '';
    this.issueService.getIssues().subscribe({
      next: (issues) => {
        this.issues = issues;
        this.isLoading = false;
      },
      error: () => {
        this.loadError = 'Erro ao carregar problemas relatados.';
        this.isLoading = false;
      }
    });
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING_APPROVAL: 'Em análise',
      OPEN:              'Aberto',
      IN_PROGRESS:       'Em andamento',
      RESOLVED:          'Resolvido',
      CLOSED:            'Encerrado'
    };
    return map[status] ?? status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING_APPROVAL: 'status-analise',
      OPEN:              'status-analise',
      IN_PROGRESS:       'status-aprovado',
      RESOLVED:          'status-concluido',
      CLOSED:            'status-concluido'
    };
    return map[status] ?? 'status-analise';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('pt-BR');
  }
}
