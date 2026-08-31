import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { catchError, of } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { SalaService, Sala, CreateSalaPayload } from '../../services/sala.service';
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
  isLoading = true;
  loadError = '';
  isModerator = false;
  showCreateForm = false;
  isCreating = false;
  createError = '';

  novaSala: CreateSalaPayload = { name: '', description: '' };

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
    this.loadSalas();
  }

  loadSalas(): void {
    this.isLoading = true;
    this.loadError = '';
    this.salaService.getSalas().pipe(
      catchError(() => {
        this.loadError = 'Erro ao carregar salas de audiência.';
        return of([] as Sala[]);
      })
    ).subscribe(salas => {
      this.salas = salas;
      this.isLoading = false;
    });
  }

  entrar(id: number): void {
    this.router.navigate(['/audiencia', id]);
  }

  criarSala(): void {
    if (!this.novaSala.name.trim()) return;
    this.isCreating = true;
    this.createError = '';
    this.salaService.createSala({
      name: this.novaSala.name.trim(),
      description: this.novaSala.description.trim()
    }).subscribe({
      next: (sala) => {
        this.isCreating = false;
        this.showCreateForm = false;
        this.novaSala = { name: '', description: '' };
        this.salas = [sala, ...this.salas];
      },
      error: () => {
        this.isCreating = false;
        this.createError = 'Erro ao criar sala. Tente novamente.';
      }
    });
  }

  encerrarSala(sala: Sala, event: Event): void {
    event.stopPropagation();
    if (!confirm(`Encerrar a sala "${sala.name}"?`)) return;
    this.salaService.encerrarSala(sala.id).subscribe({
      next: () => { this.salas = this.salas.filter(s => s.id !== sala.id); },
      error: () => {}
    });
  }

  getStatusLabel(status: string): string {
    return status === 'OPEN' ? '🔴 Aberta' : '⏹ Encerrada';
  }
}

