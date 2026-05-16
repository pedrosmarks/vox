import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ProjectService, Project } from '../../services/project.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

type FilterKey = 'todos' | 'oficiais' | 'sugeridos' | 'curtidos';

@Component({
  selector: 'app-projetos',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './projetos.component.html',
  styleUrls: ['./projetos.component.scss']
})
export class ProjetosComponent implements OnInit {
  allProjects: Project[] = [];
  filteredProjects: Project[] = [];
  activeFilter: FilterKey = 'todos';
  isLoading = true;
  errorMessage = '';

  filters: { key: FilterKey; label: string }[] = [
    { key: 'todos',     label: 'Todos os projetos' },
    { key: 'oficiais',  label: 'Projetos oficiais' },
    { key: 'sugeridos', label: 'Projetos sugeridos' },
    { key: 'curtidos',  label: 'Mais curtidos' }
  ];

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
    this.loadProjects();
  }

  loadProjects(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.allProjects = projects;
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Erro ao carregar projetos. Tente novamente.';
        this.isLoading = false;
      }
    });
  }

  setFilter(key: FilterKey): void {
    this.activeFilter = key;
    this.applyFilter();
  }

  applyFilter(): void {
    switch (this.activeFilter) {
      case 'oficiais':
        this.filteredProjects = this.allProjects.filter(p => p.isOfficial || p.type === 'OFFICIAL');
        break;
      case 'sugeridos':
        this.filteredProjects = this.allProjects.filter(p => !p.isOfficial && p.type === 'CITIZEN');
        break;
      case 'curtidos':
        this.filteredProjects = [...this.allProjects];
        break;
      default:
        this.filteredProjects = [...this.allProjects];
    }
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
