import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { IssueService, IssueReport, IssueImage } from '../../services/issue.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-problema-detalhe-moderacao',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './problema-detalhe-moderacao.component.html',
  styleUrls: ['./problema-detalhe-moderacao.component.scss']
})
export class ProblemaDetalheModeracaoComponent implements OnInit {
  issue: IssueReport | null = null;
  images: IssueImage[] = [];
  selectedImage = '';
  loading = true;
  saving = false;
  error = '';
  rejectModalOpen = false;
  rejectNote = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private issueService: IssueService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.router.navigate(['/moderacao']); return; }
    this.issueService.getIssueById(id).subscribe({
      next: issue => {
        this.issue = issue;
        this.issueService.getIssueImages(id).subscribe({
          next: images => { this.images = images; this.selectedImage = images[0]?.url ?? ''; },
          error: () => {}
        });
        this.loading = false;
      },
      error: () => { this.error = 'Ocorrência não encontrada.'; this.loading = false; }
    });
  }

  approve(): void {
    if (!this.issue || this.saving) return;
    this.saving = true;
    this.issueService.approveIssue(this.issue.id).subscribe({
      next: () => this.router.navigate(['/moderacao']),
      error: () => { this.error = 'Não foi possível aceitar a ocorrência.'; this.saving = false; }
    });
  }

  openReject(): void { this.rejectNote = ''; this.rejectModalOpen = true; }

  reject(): void {
    if (!this.issue || !this.rejectNote.trim() || this.saving) return;
    this.saving = true;
    this.issueService.updateIssueStatus(this.issue.id, 'REJECTED', this.rejectNote.trim()).subscribe({
      next: () => this.router.navigate(['/moderacao']),
      error: () => { this.error = 'Não foi possível negar a ocorrência.'; this.saving = false; }
    });
  }

  back(): void { this.router.navigate(['/moderacao']); }

  selectImage(url: string): void { this.selectedImage = url; }
}
