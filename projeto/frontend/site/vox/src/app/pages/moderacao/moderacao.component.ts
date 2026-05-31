import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ProjectService, Project, Category } from '../../services/project.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';
import { MapPickerComponent, LatLng } from '../../components/map-picker/map-picker.component';

const FALLBACK_CATEGORIES: Category[] = [
  { id: 1, name: 'Infraestrutura' },
  { id: 2, name: 'Saúde' },
  { id: 3, name: 'Educação' },
  { id: 4, name: 'Transporte' },
  { id: 5, name: 'Meio Ambiente' },
  { id: 6, name: 'Cultura e Lazer' },
  { id: 7, name: 'Segurança Pública' }
];

type ActiveTab = 'pendentes' | 'novo';

@Component({
  selector: 'app-moderacao',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent, MapPickerComponent],
  templateUrl: './moderacao.component.html',
  styleUrls: ['./moderacao.component.scss']
})
export class ModeracaoComponent implements OnInit {
  activeTab: ActiveTab = 'pendentes';

  // Pendentes
  pendingProjects: Project[] = [];
  authorNames: Map<number, string> = new Map();
  isLoadingPending = true;
  pendingError = '';
  actionInProgress: number | null = null;
  actionSuccess = '';

  // Novo projeto
  categories: Category[] = [];
  isSubmitting = false;
  submitSuccess = false;
  submitError = '';
  selectedFile: File | null = null;

  form = {
    title: '',
    description: '',
    municipalityId: 0,
    categoryId: '',
    type: 'CHAMBER',
    status: 'PUBLISHED',
    highlighted: false,
    isOfficial: true,
    neighborhood: '',
    street: '',
    number: '',
    latitude: null as number | null,
    longitude: null as number | null,
    startDate: '',
    expectedEndDate: '',
    endDate: '',
    financialAnalysis: '',
    estimatedCost: null as number | null,
    approvedBudget: null as number | null
  };

  readonly projectTypes = [
    { value: 'CHAMBER', label: 'Câmara / Prefeitura' },
    { value: 'CITIZEN', label: 'Cidadão' }
  ];

  readonly projectStatuses = [
    { value: 'PUBLISHED',            label: 'Publicado' },
    { value: 'IN_VOTING',            label: 'Em votação' },
    { value: 'SELECTED_BY_COUNCIL',  label: 'Selecionado pelo conselho' },
    { value: 'APPROVED_BY_COUNCIL',  label: 'Aprovado pelo conselho' },
    { value: 'IN_EXECUTION',         label: 'Em execução' },
    { value: 'COMPLETED',            label: 'Concluído' },
    { value: 'ARCHIVED',             label: 'Arquivado' },
    { value: 'CANCELLED',            label: 'Cancelado' }
  ];

  constructor(
    private authService: AuthService,
    private projectService: ProjectService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const role = this.authService.getUserRole();
    if (!this.authService.isLoggedIn() || (role !== 'MODERATOR' && role !== 'ADMINISTRATOR')) {
      this.router.navigate(['/projetos']);
      return;
    }
    this.form.municipalityId = this.authService.getMunicipalityId();
    this.loadPendingProjects();
    this.loadCategories();
  }

  // ── Aba: Pendentes ────────────────────────────────────────

  loadPendingProjects(): void {
    this.isLoadingPending = true;
    this.pendingError = '';
    this.projectService.getProjects('PENDING_APPROVAL').subscribe({
      next: (projects) => {
        this.pendingProjects = projects;
        this.isLoadingPending = false;
        this.loadAuthorNames(projects);
      },
      error: () => {
        this.pendingError = 'Erro ao carregar projetos pendentes.';
        this.isLoadingPending = false;
      }
    });
  }

  loadAuthorNames(projects: Project[]): void {
    const uniqueIds = [...new Set(projects.map(p => p.authorId))];
    if (!uniqueIds.length) return;
    const requests = uniqueIds.map(id =>
      this.projectService.getUserById(id).pipe(catchError(() => of({ id, name: '' })))
    );
    forkJoin(requests).subscribe(users => {
      users.forEach(u => { if (u.name) this.authorNames.set(u.id, u.name); });
    });
  }

  getAuthorName(authorId: number): string {
    return this.authorNames.get(authorId) ?? `Usuário #${authorId}`;
  }

  approve(project: Project): void {
    this.actionInProgress = project.id;
    this.actionSuccess = '';
    this.projectService.approveProject(project.id).subscribe({
      next: () => {
        this.actionInProgress = null;
        this.actionSuccess = `Projeto "${project.title}" aprovado!`;
        this.pendingProjects = this.pendingProjects.filter(p => p.id !== project.id);
      },
      error: () => { this.actionInProgress = null; }
    });
  }

  reject(project: Project): void {
    this.actionInProgress = project.id;
    this.actionSuccess = '';
    this.projectService.rejectProject(project.id).subscribe({
      next: () => {
        this.actionInProgress = null;
        this.actionSuccess = `Projeto "${project.title}" rejeitado.`;
        this.pendingProjects = this.pendingProjects.filter(p => p.id !== project.id);
      },
      error: () => { this.actionInProgress = null; }
    });
  }

  // ── Aba: Novo Projeto ─────────────────────────────────────

  loadCategories(): void {
    this.projectService.getCategories().subscribe({
      next: (cats) => { this.categories = cats.length ? cats : FALLBACK_CATEGORIES; },
      error: () => { this.categories = FALLBACK_CATEGORIES; }
    });
  }

  onLocationChange(location: LatLng): void {
    this.form.latitude = location.latitude;
    this.form.longitude = location.longitude;
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  onSubmit(): void {
    if (!this.form.title.trim() || !this.form.description.trim() || !this.form.categoryId || !this.form.startDate || !this.form.expectedEndDate) {
      this.submitError = 'Preencha todos os campos obrigatórios.';
      return;
    }

    this.isSubmitting = true;
    this.submitError = '';

    const userId = this.authService.getUserId();
    if (!userId) {
      this.submitError = 'Sessão inválida. Faça login novamente.';
      this.isSubmitting = false;
      return;
    }

    const fd = new FormData();
    fd.append('title', this.form.title.trim());
    fd.append('description', this.form.description.trim());
    fd.append('municipalityId', String(this.form.municipalityId));
    fd.append('authorId', String(userId));
    fd.append('categoryId', String(this.form.categoryId));
    fd.append('type', this.form.type);
    fd.append('status', this.form.status);
    fd.append('highlighted', String(this.form.highlighted));
    fd.append('isOfficial', 'true');
    fd.append('neighborhood', this.form.neighborhood);
    fd.append('street', this.form.street);
    fd.append('number', this.form.number);
    fd.append('startDate', this.form.startDate);
    fd.append('expectedEndDate', this.form.expectedEndDate);
    if (this.form.endDate) fd.append('endDate', this.form.endDate);
    if (this.form.financialAnalysis) fd.append('financialAnalysis', this.form.financialAnalysis);
    if (this.form.estimatedCost != null) fd.append('estimatedCost', String(this.form.estimatedCost));
    if (this.form.approvedBudget != null) fd.append('approvedBudget', String(this.form.approvedBudget));
    if (this.form.latitude != null) fd.append('latitude', String(this.form.latitude));
    if (this.form.longitude != null) fd.append('longitude', String(this.form.longitude));
    if (this.selectedFile) fd.append('file', this.selectedFile);

    this.projectService.createProject(fd).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.submitSuccess = true;
        this.resetForm();
      },
      error: (err) => {
        this.isSubmitting = false;
        this.submitError = err?.error?.message ?? 'Erro ao criar projeto. Tente novamente.';
      }
    });
  }

  resetForm(): void {
    this.form = {
      title: '', description: '',
      municipalityId: this.authService.getMunicipalityId(),
      categoryId: '', type: 'CHAMBER', status: 'PUBLISHED',
      highlighted: false, isOfficial: true,
      neighborhood: '', street: '', number: '',
      latitude: null, longitude: null,
      startDate: '', expectedEndDate: '', endDate: '',
      financialAnalysis: '', estimatedCost: null, approvedBudget: null
    };
    this.selectedFile = null;
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING_APPROVAL: 'Aguardando aprovação',
      IN_VOTING: 'Em votação',
      APPROVED: 'Aprovado',
      REJECTED: 'Rejeitado',
      IN_ANALYSIS: 'Em análise',
      COMPLETED: 'Concluído'
    };
    return map[status] ?? status;
  }
}
