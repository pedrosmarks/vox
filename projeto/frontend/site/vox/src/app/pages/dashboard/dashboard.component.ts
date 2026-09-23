import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { DashboardData, DashboardService } from '../../services/dashboard.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  data: DashboardData | null = null;
  from = this.dateOffset(-29);
  to = this.dateOffset(0);
  isLoading = false;
  errorMessage = '';
  statusChart: 'bars' | 'donut' = 'bars';
  timelineChart: 'bars' | 'line' = 'bars';
  categoryMetric: 'issues' | 'projects' = 'issues';
  moderationChart: 'volume' | 'rates' = 'volume';

  constructor(
    private authService: AuthService,
    private router: Router,
    private dashboardService: DashboardService
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    if (this.authService.getUserRole() !== 'ADMINISTRATOR') {
      this.router.navigate(['/projetos']);
      return;
    }
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.dashboardService.load({ from: this.from, to: this.to }).subscribe({
      next: data => {
        this.data = data;
        if (!data.overview) this.errorMessage = 'Não foi possível carregar os indicadores principais.';
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Não foi possível conectar ao serviço de dashboard.';
        this.isLoading = false;
      }
    });
  }

  get issueStatuses(): Array<{ label: string; value: number; color: string }> {
    const values = this.data?.overview?.issuesByStatus ?? {};
    const colors: Record<string, string> = { OPEN: '#d76535', IN_PROGRESS: '#237a8a', RESOLVED: '#438653' };
    return Object.entries(values).map(([key, value]) => ({
      label: this.label(key), value, color: colors[key] ?? '#687782'
    }));
  }

  get statusTotal(): number {
    return this.issueStatuses.reduce((total, item) => total + item.value, 0);
  }

  get statusDonut(): string {
    if (!this.statusTotal) return 'conic-gradient(#dce2e5 0deg 360deg)';
    let start = 0;
    const stops = this.issueStatuses.map(item => {
      const end = start + item.value / this.statusTotal * 360;
      const part = `${item.color} ${start}deg ${end}deg`;
      start = end;
      return part;
    });
    return `conic-gradient(${stops.join(', ')})`;
  }

  get timelinePoints(): Array<{ label: string; issues: number; projects: number; total: number; x: number; y: number }> {
    const points = this.records(this.data?.timeline?.['points']);
    const max = Math.max(1, ...points.map(point => this.number(point['total'])));
    return points.slice(-12).map((point, index, list) => ({
      label: String(point['period'] ?? '').slice(5),
      issues: this.number(point['issueCount']),
      projects: this.number(point['projectCount']),
      total: this.number(point['total']),
      x: list.length < 2 ? 150 : index / (list.length - 1) * 280,
      y: 150 - this.number(point['total']) / max * 125
    }));
  }

  get timelineLinePoints(): string {
    return this.timelinePoints.map(point => `${point.x},${point.y}`).join(' ');
  }

  get categoryItems(): Array<{ name: string; value: number; secondary: number }> {
    return this.records(this.data?.categories?.['categories']).slice(0, 6).map(item => ({
      name: String(item['categoryName'] ?? 'Sem categoria'),
      value: this.number(item[this.categoryMetric === 'issues' ? 'issueCount' : 'projectCount']),
      secondary: this.number(item[this.categoryMetric === 'issues' ? 'projectCount' : 'issueCount'])
    }));
  }

  get categoryMax(): number {
    return Math.max(1, ...this.categoryItems.map(item => item.value));
  }

  get neighborhoods(): Array<{ name: string; total: number }> {
    return (this.data?.neighborhoods ?? []).slice(0, 6).map(item => ({
      name: String(item['neighborhood'] ?? 'Não informado'),
      total: this.number(item['total'])
    }));
  }

  get topActiveUsers(): Array<{ name: string; total: number }> {
    return this.records(this.data?.engagement?.['topActiveUsers']).slice(0, 5).map(item => ({
      name: String(item['userName'] ?? 'Usuário'),
      total: this.number(item['total'])
    }));
  }

  get moderationValues(): Array<{ label: string; value: number; color: string }> {
    const moderation = this.data?.moderation;
    if (this.moderationChart === 'rates') {
      return [
        { label: 'Aprovação de ocorrências', value: this.number(moderation?.['issueApprovalRate']), color: '#237a8a' },
        { label: 'Aprovação de projetos', value: this.number(moderation?.['projectApprovalRate']), color: '#438653' }
      ];
    }
    return [
      { label: 'Pendentes', value: this.number(moderation?.['pendingIssues']) + this.number(moderation?.['pendingProjects']), color: '#d76535' },
      { label: 'Aprovados', value: this.number(moderation?.['approvedIssues']) + this.number(moderation?.['approvedProjects']), color: '#438653' },
      { label: 'Rejeitados', value: this.number(moderation?.['rejectedIssues']) + this.number(moderation?.['rejectedProjects']), color: '#687782' }
    ];
  }

  get moderationMax(): number {
    return Math.max(1, ...this.moderationValues.map(item => item.value));
  }

  get usersByRole(): Array<{ label: string; value: number }> {
    return Object.entries(this.data?.overview?.usersByRole ?? {}).map(([role, value]) => ({
      label: this.label(role), value
    }));
  }

  barWidth(value: number, max: number): string {
    return `${Math.max(value > 0 ? 3 : 0, value / max * 100)}%`;
  }

  private records(value: unknown): Array<Record<string, unknown>> {
    return Array.isArray(value) ? value as Array<Record<string, unknown>> : [];
  }

  private number(value: unknown): number {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  private label(value: string): string {
    const labels: Record<string, string> = {
      OPEN: 'Aberta', IN_PROGRESS: 'Em andamento', RESOLVED: 'Resolvida',
      PENDING: 'Pendente', APPROVED: 'Aprovada', REJECTED: 'Rejeitada',
      CITIZEN: 'Cidadãos', COUNCILOR: 'Vereadores', MODERATOR: 'Moderadores',
      ADMINISTRATOR: 'Administradores'
    };
    return labels[value] ?? value.replaceAll('_', ' ').toLowerCase();
  }

  private dateOffset(days: number): string {
    const date = new Date();
    date.setDate(date.getDate() + days);
    return [date.getFullYear(), String(date.getMonth() + 1).padStart(2, '0'), String(date.getDate()).padStart(2, '0')].join('-');
  }
}
