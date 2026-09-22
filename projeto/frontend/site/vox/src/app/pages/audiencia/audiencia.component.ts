import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SalaService, Sala } from '../../services/sala.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-audiencia',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, RouterModule],
  templateUrl: './audiencia.component.html',
  styleUrls: ['./audiencia.component.scss']
})
export class AudienciaComponent implements OnInit {
  salas: Sala[] = [];
  isModerator = false;
  isLoading = true;
  error = '';

  showCreateForm = false;
  creating = false;
  novaSala = { name: '', description: '' };

  constructor(
    private authService: AuthService,
    private salaService: SalaService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    const role = this.authService.getUserRole();
    this.isModerator = role === 'MODERATOR' || role === 'ADMINISTRATOR';
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.error = '';
    this.salaService.getSalas().subscribe({
      next: salas => {
        // Salas abertas primeiro, depois as encerradas.
        this.salas = salas.sort((a, b) => {
          if (a.status === b.status) return b.id - a.id;
          return a.status === 'OPEN' ? -1 : 1;
        });
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Erro ao carregar as salas de audiência.';
        this.isLoading = false;
      }
    });
  }

  entrar(sala: Sala): void {
    if (sala.status !== 'OPEN') return;
    this.router.navigate(['/audiencia', sala.id]);
  }

  criarSala(): void {
    if (this.creating) return;
    if (!this.novaSala.name.trim()) return;
    this.creating = true;
    this.salaService
      .createSala({
        name: this.novaSala.name.trim(),
        description: this.novaSala.description.trim()
      })
      .subscribe({
        next: () => {
          this.creating = false;
          this.showCreateForm = false;
          this.novaSala = { name: '', description: '' };
          this.load();
        },
        error: () => {
          this.creating = false;
          this.error = 'Não foi possível criar a sala. Verifique suas permissões.';
        }
      });
  }

  encerrarSala(sala: Sala, event: Event): void {
    event.stopPropagation();
    if (!confirm(`Encerrar a sala "${sala.name}" para todos os participantes?`)) return;
    this.salaService.encerrarSala(sala.id).subscribe({
      next: () => this.load(),
      error: () => (this.error = 'Não foi possível encerrar a sala.')
    });
  }

  statusLabel(status: string): string {
    return status === 'OPEN' ? '🔴 Ao vivo' : '⏹ Encerrada';
  }

  formatDate(iso: string): string {
    if (!iso) return '';
    const d = new Date(iso);
    if (isNaN(d.getTime())) return '';
    return d.toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
