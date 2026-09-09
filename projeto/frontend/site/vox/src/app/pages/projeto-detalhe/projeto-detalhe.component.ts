import { Component, OnInit, HostListener, ElementRef } from '@angular/core';
import { CommonModule, DatePipe, CurrencyPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProjectService, Project, ProjectImage } from '../../services/project.service';
import { AuthService } from '../../services/auth.service';
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
  isModerator = false;
  signed = false;
  signing = false;
  signatureCount = 0;
  exportMenuOpen = false;

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.exportMenuOpen) return;
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.exportMenuOpen = false;
    }
  }

  constructor(
    private elementRef: ElementRef,
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const role = this.authService.getUserRole();
    this.isModerator = role === 'MODERATOR' || role === 'ADMINISTRATOR';
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

    this.loadSignatureState(project.id);
  }

  /** Carrega se o usuário assinou e a contagem total de assinaturas. */
  private loadSignatureState(id: number): void {
    this.projectService
      .hasSignedProject(id)
      .pipe(catchError(() => of({ signed: false })))
      .subscribe(res => (this.signed = res.signed));

    this.projectService
      .getSignatureCount(id)
      .pipe(catchError(() => of({ total: 0 })))
      .subscribe(res => (this.signatureCount = res.total));
  }

  selectImage(url: string): void {
    this.selectedImage = url;
  }

  goBack(): void {
    this.router.navigate(['/projetos']);
  }

  promoteProject(): void {
    if (this.project) {
      this.router.navigate(['/moderacao'], { queryParams: { promoteId: this.project.id } });
    }
  }

  toggleSign(): void {
    if (!this.project || this.signing) return;
    const id = this.project.id;
    const wasSigned = this.signed;
    this.signing = true;

    // Atualização otimista.
    this.signed = !wasSigned;
    this.signatureCount += wasSigned ? -1 : 1;

    const request$ = wasSigned
      ? this.projectService.unsignProject(id)
      : this.projectService.signProject(id);

    request$.subscribe({
      next: () => (this.signing = false),
      error: () => {
        // Reverte em caso de falha.
        this.signed = wasSigned;
        this.signatureCount += wasSigned ? 1 : -1;
        this.signing = false;
      }
    });
  }

  // ── Exportação ─────────────────────────────────────────────

  toggleExportMenu(): void {
    this.exportMenuOpen = !this.exportMenuOpen;
  }

  /** Pares rótulo/valor usados nos dois formatos de exportação. */
  private buildExportRows(): { label: string; value: string }[] {
    const p = this.project;
    if (!p) return [];
    const fmtDate = (d?: string | null) =>
      d ? new Date(d).toLocaleDateString('pt-BR') : '—';
    const fmtMoney = (v?: number | null) =>
      v != null && v > 0
        ? v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
        : '—';

    return [
      { label: 'ID', value: String(p.id) },
      { label: 'Título', value: p.title },
      { label: 'Descrição', value: p.description },
      { label: 'Status', value: this.getStatusLabel(p.status) },
      { label: 'Tipo', value: this.getTypeLabel(p) },
      { label: 'Categoria', value: this.categoryName || '—' },
      { label: 'Autor', value: this.authorName || `Usuário #${p.authorId}` },
      { label: 'Bairro', value: p.neighborhood || '—' },
      {
        label: 'Endereço',
        value: p.street ? `${p.street}${p.number ? ', ' + p.number : ''}` : '—'
      },
      { label: 'Data de início', value: fmtDate(p.startDate) },
      { label: 'Previsão de término', value: fmtDate(p.expectedEndDate) },
      { label: 'Data de conclusão', value: fmtDate(p.endDate) },
      { label: 'Criado em', value: fmtDate(p.createdAt) },
      { label: 'Custo estimado', value: fmtMoney(p.estimatedCost) },
      { label: 'Orçamento aprovado', value: fmtMoney(p.approvedBudget) },
      { label: 'Assinaturas de apoio', value: String(this.signatureCount) }
    ];
  }

  private safeFileName(): string {
    const base = (this.project?.title || 'projeto')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-zA-Z0-9]+/g, '_')
      .replace(/^_+|_+$/g, '')
      .toLowerCase();
    return `projeto_${this.project?.id ?? ''}_${base}`.replace(/_+$/g, '');
  }

  exportCsv(): void {
    this.exportMenuOpen = false;
    const rows = this.buildExportRows();
    if (rows.length === 0) return;

    const escape = (v: string) => `"${(v ?? '').replace(/"/g, '""')}"`;
    const lines = [
      `${escape('Campo')},${escape('Valor')}`,
      ...rows.map(r => `${escape(r.label)},${escape(r.value)}`)
    ];
    // BOM para o Excel reconhecer UTF-8 (acentuação).
    const csv = '\uFEFF' + lines.join('\r\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    this.triggerDownload(blob, `${this.safeFileName()}.csv`);
  }

  exportPdf(): void {
    this.exportMenuOpen = false;
    const p = this.project;
    if (!p) return;
    const rows = this.buildExportRows();

    const esc = (s: string) =>
      (s ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');

    const rowsHtml = rows
      .map(
        r => `<tr><th>${esc(r.label)}</th><td>${esc(r.value)}</td></tr>`
      )
      .join('');

    const html = `<!DOCTYPE html>
<html lang="pt-BR"><head><meta charset="utf-8" />
<title>${esc(p.title)}</title>
<style>
  * { font-family: Arial, Helvetica, sans-serif; color: #1b3f8b; }
  body { padding: 32px; }
  h1 { font-size: 20px; margin: 0 0 4px; }
  .sub { color: #6b7280; font-size: 12px; margin: 0 0 20px; }
  table { width: 100%; border-collapse: collapse; }
  th, td { text-align: left; vertical-align: top; padding: 8px 10px; border-bottom: 1px solid #e5e7eb; font-size: 13px; }
  th { width: 200px; color: #1b3f8b; background: #f0f4ff; }
  td { color: #111827; }
  .foot { margin-top: 24px; font-size: 11px; color: #9ca3af; }
</style></head>
<body>
  <h1>${esc(p.title)}</h1>
  <p class="sub">VOX — Detalhes do Projeto</p>
  <table>${rowsHtml}</table>
  <p class="foot">Exportado em ${new Date().toLocaleString('pt-BR')}</p>
  <script>window.onload = function () { window.print(); };</script>
</body></html>`;

    const win = window.open('', '_blank');
    if (!win) {
      alert('Habilite pop-ups para exportar o PDF.');
      return;
    }
    win.document.open();
    win.document.write(html);
    win.document.close();
  }

  private triggerDownload(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
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
