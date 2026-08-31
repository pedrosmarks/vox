import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService, LogEntry } from '../../services/auth.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

/** Tela do administrador para auditoria de ações do sistema. */
@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss']
})
export class LogsComponent implements OnInit {
  logs: LogEntry[] = [];
  isLoading = true;
  loadError = '';

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

  loadLogs(): void {
    this.isLoading = true;
    this.loadError = '';
    this.authService.getLogs().subscribe({
      next: (logs) => {
        this.logs = logs;
        this.isLoading = false;
      },
      error: () => {
        this.loadError = 'Não foi possível carregar os logs. Verifique se o endpoint /api/logs está disponível no backend.';
        this.isLoading = false;
      }
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('pt-BR');
  }
}
