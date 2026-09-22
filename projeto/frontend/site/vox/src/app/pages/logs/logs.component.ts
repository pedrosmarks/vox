import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, LogEntry, LogPage } from '../../services/auth.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

/** Histórico de auditoria do município do administrador autenticado. */
@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss']
})
export class LogsComponent implements OnInit {
  logs: LogEntry[] = [];
  page = 0;
  size = 20;
  totalElements = 0;
  userId = '';
  method = '';
  from = '';
  to = '';
  isLoading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const role = this.authService.getUserRole();
    if (!this.authService.isLoggedIn() || role !== 'ADMINISTRATOR') {
      this.router.navigate(['/projetos']);
      return;
    }
    this.loadLogs();
  }

  loadLogs(page = 0): void {
    const userId = this.userId.trim() ? Number(this.userId) : undefined;
    if (userId !== undefined && (!Number.isInteger(userId) || userId < 0)) {
      this.errorMessage = 'Informe um ID de usuário válido.';
      return;
    }
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
      userId,
      method: this.method || undefined,
      from: this.from || undefined,
      to: this.to || undefined
    }).subscribe({
      next: (result: LogPage) => {
        this.logs = result.content ?? [];
        this.page = result.page;
        this.size = result.size;
        this.totalElements = result.totalElements;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Não foi possível carregar os logs de auditoria.';
        this.isLoading = false;
      }
    });
  }

  clearFilters(): void {
    this.userId = '';
    this.method = '';
    this.from = '';
    this.to = '';
    this.loadLogs();
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

  formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString('pt-BR');
  }

  methodLabel(method: string): string {
    return method === 'DELETE' ? 'DELETE' : method;
  }
}
