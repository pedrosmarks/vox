import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { IssueService, IssueReport } from '../../services/issue.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { issueStatusLabel, statusClass } from '../../utils/status-labels';

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
