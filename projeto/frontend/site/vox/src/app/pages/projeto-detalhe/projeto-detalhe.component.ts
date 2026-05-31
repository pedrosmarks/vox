import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProjectService, Project, ProjectImage } from '../../services/project.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-projeto-detalhe',
  standalone: true,
  imports: [CommonModule, NavbarComponent, DatePipe, CurrencyPipe],
  templateUrl: './projeto-detalhe.component.html',
  styleUrls: ['./projeto-detalhe.component.scss']
})
export class ProjetoDetalheComponent implements OnInit {
  project: Project | null = null;
  images: ProjectImage[] = [];
  categoryName = '';
  authorName = '';
  isLoading = true;
  errorMessage = '';
  selectedImage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/projetos']);
      return;
    }
    this.loadProject(id);
  }

  loadProject(id: number): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.projectService.getProjectById(id).subscribe({
      next: (project) => {
        this.project = project;
        this.loadExtras(project);
      },
      error: () => {
        this.errorMessage = 'Projeto não encontrado ou erro ao carregar.';
        this.isLoading = false;
      }
    });
  }

  loadExtras(project: Project): void {
    const images$ = this.projectService.getProjectImages(project.id).pipe(catchError(() => of([])));
    const category$ = this.projectService.getCategoryById(project.categoryId).pipe(catchError(() => of(null)));
    const author$ = (project.authorId)
      ? this.projectService.getUserById(project.authorId).pipe(catchError(() => of(null)))
      : of(null);

    forkJoin([images$, category$, author$]).subscribe(([images, category, author]) => {
      this.images = images as ProjectImage[];
      this.selectedImage = this.images[0]?.url ?? '';
      this.categoryName = (category as any)?.name ?? '';
      this.authorName = (author as any)?.name ?? '';
      this.isLoading = false;
    });
  }

  selectImage(url: string): void {
    this.selectedImage = url;
  }

  goBack(): void {
    this.router.navigate(['/projetos']);
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING_APPROVAL: 'Em análise',
      IN_VOTING:        'Em votação',
      APPROVED:         'Aprovado',
      REJECTED:         'Rejeitado',
      IN_ANALYSIS:      'Em análise',
      COMPLETED:        'Concluído'
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

  getTypeLabel(project: Project): string {
    return project.isOfficial || project.type === 'OFFICIAL' ? 'Projeto Oficial' : 'Projeto Sugerido';
  }

  getTypeClass(project: Project): string {
    return project.isOfficial || project.type === 'OFFICIAL' ? 'type-oficial' : 'type-sugerido';
  }
}
