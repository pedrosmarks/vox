import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProjectService, Project, ProjectImage } from '../../services/project.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-projeto-detalhe-moderacao',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './projeto-detalhe-moderacao.component.html',
  styleUrls: ['./projeto-detalhe-moderacao.component.scss']
})
export class ProjetoDetalheModeracaoComponent implements OnInit {
  project: Project | null = null;
  images: ProjectImage[] = [];
  selectedImage = '';
  loading = true;
  saving = false;
  error = '';
  rejectModalOpen = false;
  rejectNote = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/moderacao']);
      return;
    }
    this.projectService.getProjectById(id).subscribe({
      next: project => {
        this.project = project;
        this.projectService.getProjectImages(id).subscribe({
          next: images => { this.images = images; this.selectedImage = images[0]?.url ?? ''; },
          error: () => {}
        });
        this.loading = false;
      },
      error: () => { this.error = 'Projeto não encontrado.'; this.loading = false; }
    });
  }

  approve(): void {
    if (!this.project || this.saving) return;
    this.saving = true;
    this.projectService.approveProject(this.project.id).subscribe({
      next: () => this.router.navigate(['/moderacao']),
      error: () => { this.error = 'Não foi possível aceitar o projeto.'; this.saving = false; }
    });
  }

  openReject(): void {
    this.rejectNote = '';
    this.rejectModalOpen = true;
  }

  reject(): void {
    if (!this.project || !this.rejectNote.trim() || this.saving) return;
    this.saving = true;
    this.projectService.rejectProject(this.project.id, this.rejectNote.trim()).subscribe({
      next: () => this.router.navigate(['/moderacao']),
      error: () => { this.error = 'Não foi possível negar o projeto.'; this.saving = false; }
    });
  }

  back(): void { this.router.navigate(['/moderacao']); }

  selectImage(url: string): void { this.selectedImage = url; }
}
