import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, UserProfile } from '../../services/auth.service';
import { ProjectService, Category } from '../../services/project.service';
import { IssueService, IssueReport } from '../../services/issue.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { MapPickerComponent, LatLng, AddressResult } from '../../components/map-picker/map-picker.component';

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
  selector: 'app-relatar-problema',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, MapPickerComponent],
  templateUrl: './relatar-problema.component.html',
  styleUrls: ['./relatar-problema.component.scss']
})
export class RelatarProblemaComponent implements OnInit {
  showForm = false;
  isLoadingIssues = true;
  isSubmitting = false;
  submitSuccess = false;
  submitError = '';
  loadError = '';

  myIssues: IssueReport[] = [];
  categories: Category[] = [];
  councilors: UserProfile[] = [];

  form = {
    title: '',
    categoryId: '',
    councilorId: '',
    description: '',
    file: null as File | null,
    latitude: null as number | null,
    longitude: null as number | null,
    street: '',
    number: '',
    neighborhood: ''
  };

  private userId: number | null = null;

  constructor(
    private authService: AuthService,
    private projectService: ProjectService,
    private issueService: IssueService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.userId = this.authService.getUserId();
    this.loadCategories();
    this.loadCouncilors();
    this.loadMyIssues();
  }

  loadCouncilors(): void {
    this.authService.getCouncilors().subscribe({
      next: (councilors) => { this.councilors = councilors; },
      error: () => { this.councilors = []; }
    });
  }

  loadCategories(): void {
    this.projectService.getCategories().subscribe({
      next: (cats) => { this.categories = cats.length ? cats : FALLBACK_CATEGORIES; },
      error: () => { this.categories = FALLBACK_CATEGORIES; }
    });
  }

  loadMyIssues(): void {
    this.isLoadingIssues = true;
    this.loadError = '';
    this.issueService.getMyIssues().subscribe({
      next: (issues) => {
        this.myIssues = issues;
        this.isLoadingIssues = false;
      },
      error: () => {
        this.loadError = 'Erro ao carregar suas ocorrências.';
        this.isLoadingIssues = false;
      }
    });
  }

  openForm(): void {
    this.showForm = true;
    this.submitSuccess = false;
    this.submitError = '';
    this.form = { title: '', categoryId: '', councilorId: '', description: '', file: null, latitude: null, longitude: null, street: '', number: '', neighborhood: '' };
  }

  onLocationChange(location: LatLng): void {
    this.form.latitude = location.latitude;
    this.form.longitude = location.longitude;
  }

  onAddressChange(address: AddressResult): void {
    this.form.street = address.street;
    this.form.number = address.number;
    this.form.neighborhood = address.neighborhood;
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
    if (!this.form.title.trim() || !this.form.categoryId || !this.form.description.trim() || !this.form.councilorId) {
      this.submitError = 'Preencha todos os campos obrigatórios.';
      return;
    }

    this.isSubmitting = true;
    this.submitError = '';

    const fd = new FormData();
    fd.append('municipalityId', String(this.authService.getMunicipalityId()));
    fd.append('categoryId', this.form.categoryId);
    fd.append('councilorId', this.form.councilorId);
    fd.append('title', this.form.title.trim());
    fd.append('description', this.form.description.trim());
    fd.append('status', 'PENDING_APPROVAL');
    if (this.userId !== null) {
      fd.append('authorId', String(this.userId));
    }
    if (this.form.latitude !== null && this.form.longitude !== null) {
      fd.append('latitude', String(this.form.latitude));
      fd.append('longitude', String(this.form.longitude));
    }
    if (this.form.street) fd.append('street', this.form.street);
    if (this.form.number) fd.append('number', this.form.number);
    if (this.form.neighborhood) fd.append('neighborhood', this.form.neighborhood);
    if (this.form.file) {
      fd.append('file', this.form.file);
    }

    this.issueService.createIssue(fd).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.submitSuccess = true;
        this.showForm = false;
        this.loadMyIssues();
      },
      error: (err) => {
        this.isSubmitting = false;
        if (err.status === 403) {
          this.submitError = 'Sem permissão para relatar problemas.';
        } else {
          this.submitError = 'Erro ao enviar ocorrência. Tente novamente.';
        }
      }
    });
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING_APPROVAL: 'Em análise',
      APPROVED:          'Aprovada',
      REJECTED:          'Rejeitada',
      IN_ANALYSIS:       'Em análise',
      RESOLVED:          'Resolvida'
    };
    return map[status] ?? status;
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING_APPROVAL: 'status-analise',
      APPROVED:          'status-aprovado',
      REJECTED:          'status-rejeitado',
      IN_ANALYSIS:       'status-analise',
      RESOLVED:          'status-concluido'
    };
    return map[status] ?? 'status-analise';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    const [year, month, day] = dateStr.split('T')[0].split('-');
    return `${day}/${month}/${year}`;
  }
}
