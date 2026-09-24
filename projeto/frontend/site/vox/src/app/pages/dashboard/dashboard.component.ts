import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import * as maplibregl from 'maplibre-gl';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { DashboardData, DashboardService } from '../../services/dashboard.service';
import { AccessibilityMode, SettingsService } from '../../services/settings.service';
import { NavbarComponent } from '../../components/navbar/navbar.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('hotspotMap') hotspotMap!: ElementRef<HTMLDivElement>;

  data: DashboardData | null = null;
  from = this.dateOffset(-29);
  to = this.dateOffset(0);
  isLoading = false;
  errorMessage = '';
  statusChart: 'bars' | 'donut' = 'bars';
  timelineChart: 'bars' | 'line' = 'bars';
  categoryMetric: 'issues' | 'projects' = 'issues';
  moderationChart: 'volume' | 'rates' = 'volume';
  hotspotType: 'issues' | 'projects' = 'issues';

  private map: maplibregl.Map | null = null;
  private mapReady = false;
  private accessibilityMode: AccessibilityMode = 'NONE';
  private settingsSubscription: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private dashboardService: DashboardService,
    private settingsService: SettingsService
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
    this.settingsSubscription = this.settingsService.settings$.subscribe(settings => {
      this.accessibilityMode = settings.accessibilityMode;
      this.updateHotspotPalette();
    });
    this.load();
  }

  ngAfterViewInit(): void {
    this.map = new maplibregl.Map({
      container: this.hotspotMap.nativeElement,
      style: 'https://basemaps.cartocdn.com/gl/positron-gl-style/style.json',
      center: [-47.9292, -15.7801],
      zoom: 4
    });
    this.map.addControl(new maplibregl.NavigationControl(), 'top-right');
    this.map.on('load', () => {
      if (!this.map) return;
      this.map.addSource('hotspots', { type: 'geojson', data: this.hotspotGeoJson() });
      this.map.addLayer({
        id: 'hotspot-heat-issues',
        type: 'heatmap',
        source: 'hotspots',
        filter: ['==', ['get', 'category'], 'issues'],
        paint: {
          'heatmap-weight': ['interpolate', ['linear'], ['get', 'count'], 0, 0, 20, 1],
          'heatmap-intensity': ['interpolate', ['linear'], ['zoom'], 0, 1, 9, 2.5],
          'heatmap-radius': ['interpolate', ['linear'], ['zoom'], 0, 10, 9, 28],
          'heatmap-opacity': ['interpolate', ['linear'], ['zoom'], 7, 0.9, 12, 0.35],
          'heatmap-color': [
            'interpolate', ['linear'], ['heatmap-density'],
            0, 'rgba(37,99,235,0)',
            0.25, 'rgba(37,99,235,0.28)',
            0.55, 'rgba(37,99,235,0.58)',
            1, 'rgba(30,64,175,0.92)'
          ]
        }
      });
      this.map.addLayer({
        id: 'hotspot-heat-projects',
        type: 'heatmap',
        source: 'hotspots',
        filter: ['==', ['get', 'category'], 'projects'],
        paint: {
          'heatmap-weight': ['interpolate', ['linear'], ['get', 'count'], 0, 0, 20, 1],
          'heatmap-intensity': ['interpolate', ['linear'], ['zoom'], 0, 1, 9, 2.5],
          'heatmap-radius': ['interpolate', ['linear'], ['zoom'], 0, 10, 9, 28],
          'heatmap-opacity': ['interpolate', ['linear'], ['zoom'], 7, 0.9, 12, 0.35],
          'heatmap-color': [
            'interpolate', ['linear'], ['heatmap-density'],
            0, 'rgba(220,38,38,0)',
            0.25, 'rgba(220,38,38,0.28)',
            0.55, 'rgba(220,38,38,0.58)',
            1, 'rgba(153,27,27,0.92)'
          ]
        }
      });
      this.map.addLayer({
        id: 'hotspot-circles-issues',
        type: 'circle',
        source: 'hotspots',
        filter: ['==', ['get', 'category'], 'issues'],
        minzoom: 9,
        paint: {
          'circle-radius': ['interpolate', ['linear'], ['get', 'count'], 1, 6, 10, 12, 25, 19],
          'circle-color': '#2563eb',
          'circle-opacity': 0.72,
          'circle-stroke-color': '#fff',
          'circle-stroke-width': 1.5
        }
      });
      this.map.addLayer({
        id: 'hotspot-circles-projects',
        type: 'circle',
        source: 'hotspots',
        filter: ['==', ['get', 'category'], 'projects'],
        minzoom: 9,
        paint: {
          'circle-radius': ['interpolate', ['linear'], ['get', 'count'], 1, 6, 10, 12, 25, 19],
          'circle-color': '#dc2626',
          'circle-opacity': 0.72,
          'circle-stroke-color': '#fff',
          'circle-stroke-width': 1.5
        }
      });
      this.mapReady = true;
      this.updateHotspotPalette();
      this.updateHotspots();
    });
  }

  ngOnDestroy(): void {
    this.settingsSubscription?.unsubscribe();
    this.map?.remove();
  }

  load(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.dashboardService.load({ from: this.from, to: this.to }).subscribe({
      next: data => {
        this.data = data;
        this.updateHotspots();
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

  get hotspots(): Array<Record<string, unknown>> {
    return this.data?.hotspots ?? [];
  }

  get hotspotTotal(): number {
    return this.hotspots.reduce((total, item) => total + this.hotspotCount(item), 0);
  }

  get hotspotZones(): number {
    return this.hotspots.filter(item => this.hotspotCount(item) > 0).length;
  }

  get hotspotEmptyMessage(): string {
    return this.hotspotType === 'issues'
      ? 'Sem ocorrências aprovadas neste período.'
      : 'Sem projetos aprovados neste período.';
  }

  selectHotspotType(type: 'issues' | 'projects'): void {
    this.hotspotType = type;
    this.updateHotspots();
  }

  get issueResolutionRate(): number | null {
    return this.data?.overview?.issueResolutionRate ?? null;
  }

  get pendingModerationProjects(): number {
    return this.data?.overview?.pendingModerationProjects ?? 0;
  }

  get publishedProjects(): number {
    return this.data?.overview?.publishedProjects ?? 0;
  }

  get budgetExecutionRate(): unknown {
    return this.data?.projectLifecycle?.['budgetExecutionRate'];
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

  private hotspotGeoJson() {
    const features: Array<{
      type: 'Feature';
      geometry: { type: 'Point'; coordinates: [number, number] };
      properties: { category: 'issues' | 'projects'; count: number };
    }> = [];
    for (const item of this.hotspots) {
      const coordinates: [number, number] = [
        this.number(item['longitude']),
        this.number(item['latitude'])
      ];
      const issueCount = this.number(item['issueCount']);
      const projectCount = this.number(item['projectCount']);
      if (issueCount > 0) {
        features.push({
          type: 'Feature',
          geometry: { type: 'Point', coordinates },
          properties: { category: 'issues', count: issueCount }
        });
      }
      if (projectCount > 0) {
        features.push({
          type: 'Feature',
          geometry: { type: 'Point', coordinates },
          properties: { category: 'projects', count: projectCount }
        });
      }
    }
    return {
      type: 'FeatureCollection' as const,
      features
    };
  }

  private hotspotCount(item: Record<string, unknown>): number {
    return this.number(item[this.hotspotType === 'issues' ? 'issueCount' : 'projectCount']);
  }

  private updateHotspotPalette(): void {
    if (!this.mapReady || !this.map) return;
    const highContrast = this.accessibilityMode === 'HIGH_CONTRAST';
    const dark = this.accessibilityMode === 'DARK';
    const issueColor = highContrast ? '#ffff00' : dark ? '#60a5fa' : '#2563eb';
    const projectColor = highContrast ? '#ffff00' : dark ? '#fb7185' : '#dc2626';
    const outlineColor = highContrast ? '#000000' : dark ? '#111827' : '#ffffff';

    this.map.setPaintProperty('hotspot-circles-issues', 'circle-color', issueColor);
    this.map.setPaintProperty('hotspot-circles-projects', 'circle-color', projectColor);
    this.map.setPaintProperty('hotspot-circles-issues', 'circle-stroke-color', outlineColor);
    this.map.setPaintProperty('hotspot-circles-projects', 'circle-stroke-color', outlineColor);
    if (highContrast) {
      const yellowRamp = [
        'interpolate', ['linear'], ['heatmap-density'],
        0, 'rgba(255,255,0,0)',
        0.2, 'rgba(255,255,0,0.4)',
        0.55, 'rgba(255,255,0,0.75)',
        1, 'rgba(255,255,0,1)'
      ];
      this.map.setPaintProperty('hotspot-heat-issues', 'heatmap-color', yellowRamp);
      this.map.setPaintProperty('hotspot-heat-projects', 'heatmap-color', yellowRamp);
      this.map.setPaintProperty('hotspot-heat-issues', 'heatmap-opacity', 1);
      this.map.setPaintProperty('hotspot-heat-projects', 'heatmap-opacity', 1);
      this.map.setPaintProperty('hotspot-circles-issues', 'circle-opacity', 0.95);
      this.map.setPaintProperty('hotspot-circles-projects', 'circle-opacity', 0.95);
      return;
    }

    const issueRamp = dark
      ? ['interpolate', ['linear'], ['heatmap-density'], 0, 'rgba(96,165,250,0)', 0.25, 'rgba(96,165,250,0.35)', 0.55, 'rgba(96,165,250,0.65)', 1, 'rgba(30,64,175,0.95)']
      : ['interpolate', ['linear'], ['heatmap-density'], 0, 'rgba(37,99,235,0)', 0.25, 'rgba(37,99,235,0.28)', 0.55, 'rgba(37,99,235,0.58)', 1, 'rgba(30,64,175,0.92)'];
    const projectRamp = dark
      ? ['interpolate', ['linear'], ['heatmap-density'], 0, 'rgba(251,113,133,0)', 0.25, 'rgba(251,113,133,0.35)', 0.55, 'rgba(251,113,133,0.65)', 1, 'rgba(190,24,93,0.95)']
      : ['interpolate', ['linear'], ['heatmap-density'], 0, 'rgba(220,38,38,0)', 0.25, 'rgba(220,38,38,0.28)', 0.55, 'rgba(220,38,38,0.58)', 1, 'rgba(153,27,27,0.92)'];
    this.map.setPaintProperty('hotspot-heat-issues', 'heatmap-color', issueRamp);
    this.map.setPaintProperty('hotspot-heat-projects', 'heatmap-color', projectRamp);
    this.map.setPaintProperty('hotspot-heat-issues', 'heatmap-opacity', ['interpolate', ['linear'], ['zoom'], 7, 0.9, 12, 0.35]);
    this.map.setPaintProperty('hotspot-heat-projects', 'heatmap-opacity', ['interpolate', ['linear'], ['zoom'], 7, 0.9, 12, 0.35]);
    this.map.setPaintProperty('hotspot-circles-issues', 'circle-opacity', 0.72);
    this.map.setPaintProperty('hotspot-circles-projects', 'circle-opacity', 0.72);
  }

  private updateHotspots(): void {
    if (!this.mapReady || !this.map) return;
    const source = this.map.getSource('hotspots') as maplibregl.GeoJSONSource | undefined;
    source?.setData(this.hotspotGeoJson());
    const visibility = this.hotspotType === 'issues' ? 'visible' : 'none';
    this.map.setLayoutProperty('hotspot-heat-issues', 'visibility', visibility);
    this.map.setLayoutProperty('hotspot-circles-issues', 'visibility', visibility);
    const projectVisibility = this.hotspotType === 'projects' ? 'visible' : 'none';
    this.map.setLayoutProperty('hotspot-heat-projects', 'visibility', projectVisibility);
    this.map.setLayoutProperty('hotspot-circles-projects', 'visibility', projectVisibility);

    const visibleHotspots = this.hotspots.filter(item => this.hotspotCount(item) > 0);
    if (visibleHotspots.length === 1) {
      const item = visibleHotspots[0];
      this.map.flyTo({
        center: [this.number(item['longitude']), this.number(item['latitude'])],
        zoom: 12,
        duration: 500
      });
    } else if (visibleHotspots.length > 1) {
      const bounds = new maplibregl.LngLatBounds();
      for (const item of visibleHotspots) {
        bounds.extend([this.number(item['longitude']), this.number(item['latitude'])]);
      }
      this.map.fitBounds(bounds, { padding: 48, maxZoom: 12, duration: 500 });
    }
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
