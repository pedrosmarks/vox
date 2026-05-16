import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ProjectService, Project, Category } from '../../services/project.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

const FALLBACK_CATEGORIES: Category[] = [
  { id: 1, name: 'Infraestrutura' },
  { id: 2, name: 'Saúde' },
  { id: 3, name: 'Educação' },
  { id: 4, name: 'Transporte' },
  { id: 5, name: 'Meio Ambiente' },
  { id: 6, name: 'Cultura e Lazer' },
  { id: 7, name: 'Segurança Pública' }
];

@Component({
  selector: 'app-sugestoes',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './sugestoes.component.html',
  styleUrls: ['./sugestoes.component.scss']
})
export class SugestoesComponent implements OnInit {
  showForm = false;
  isLoadingSuggestions = true;
  isSubmitting = false;
  submitSuccess = false;
  submitError = '';
  loadError = '';

  mySuggestions: Project[] = [];
  categories: Category[] = [];

  form = {
    title: '',
    categoryId: '',
    description: '',
    file: null as File | null
  };

  private userId: number | null = null;

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
    this.userId = this.authService.getUserId();
    this.loadCategories();
    this.loadMySuggestions();
  }

  loadCategories(): void {
    this.projectService.getCategories().subscribe({
      next: (cats) => { this.categories = cats.length ? cats : FALLBACK_CATEGORIES; },
      error: () => { this.categories = FALLBACK_CATEGORIES; }
    });
  }

  loadMySuggestions(): void {
    this.isLoadingSuggestions = true;
    this.loadError = '';
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.mySuggestions = this.userId !== null
          ? projects.filter(p => p.authorId === this.userId)
          : projects;
        this.isLoadingSuggestions = false;
      },
      error: () => {
        this.loadError = 'Erro ao carregar suas sugestões.';
        this.isLoadingSuggestions = false;
      }
    });
  }

  openForm(): void {
    this.showForm = true;
    this.submitSuccess = false;
    this.submitError = '';
    this.form = { title: '', categoryId: '', description: '', file: null };
  }

  cancelForm(): void {
    this.showForm = false;
    this.submitError = '';
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.form.file = input.files?.[0] ?? null;
  }

  onSubmit(): void {
    if (!this.form.title.trim() || !this.form.categoryId || !this.form.description.trim()) {
      this.submitError = 'Preencha todos os campos obrigatórios.';
      return;
    }

    this.isSubmitting = true;
    this.submitError = '';

    const fd = new FormData();
    fd.append('municipalityId', String(this.authService.getMunicipalityId()));
    fd.append('categoryId', this.form.categoryId);
    fd.append('type', 'CITIZEN');
    fd.append('title', this.form.title.trim());
    fd.append('description', this.form.description.trim());
    fd.append('status', 'PENDING_APPROVAL');
    if (this.userId !== null) {
      fd.append('authorId', String(this.userId));
    }
    fd.append('highlighted', 'false');
    fd.append('isOfficial', 'false');
    if (this.form.file) {
      fd.append('file', this.form.file);
    }

    this.projectService.createProject(fd).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.submitSuccess = true;
        this.showForm = false;
        this.loadMySuggestions();
      },
      error: (err) => {
        this.isSubmitting = false;
        if (err.status === 403) {
          this.submitError = 'Sem permissão para criar projetos.';
        } else {
          this.submitError = 'Erro ao enviar sugestão. Tente novamente.';
        }
      }
    });
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING_APPROVAL: 'Em análise',
      IN_VOTING:        'Em votação',
      APPROVED:         'Aprovada para votação',
      REJECTED:         'Rejeitada',
      IN_ANALYSIS:      'Em análise',
      COMPLETED:        'Concluída'
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

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const [year, month, day] = dateStr.split('-');
    return `${day}/${month}/${year}`;
  }
}
