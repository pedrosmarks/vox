import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, forkJoin, of } from 'rxjs';

export interface DashboardOverview {
  totalIssues: number;
  approvedIssues: number;
  pendingModerationIssues: number;
  resolvedIssues: number;
  issuesByStatus: Record<string, number>;
  totalProjects: number;
  publishedProjects: number;
  pendingModerationProjects: number;
  projectsByStatus: Record<string, number>;
  totalUsers: number;
  usersByRole: Record<string, number>;
  issueResolutionRate: number;
}

export interface DashboardFilters {
  from: string;
  to: string;
}

export interface DashboardData {
  overview: DashboardOverview | null;
  moderation: Record<string, unknown> | null;
  engagement: Record<string, unknown> | null;
  categories: Record<string, unknown> | null;
  timeline: Record<string, unknown> | null;
  neighborhoods: Array<Record<string, unknown>> | null;
  projectLifecycle: Record<string, unknown> | null;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly apiUrl = 'http://localhost:8080/api/admin/dashboard';

  constructor(private http: HttpClient) {}

  load(filters: DashboardFilters): Observable<DashboardData> {
    const params = new HttpParams().set('from', filters.from).set('to', filters.to);
    const optional = <T>(request: Observable<T>) => request.pipe(catchError(() => of(null)));

    return forkJoin({
      overview: optional(this.http.get<DashboardOverview>(`${this.apiUrl}/overview`, { params })),
      moderation: optional(this.http.get<Record<string, unknown>>(`${this.apiUrl}/moderacao`, { params })),
      engagement: optional(this.http.get<Record<string, unknown>>(`${this.apiUrl}/engajamento`, { params })),
      categories: optional(this.http.get<Record<string, unknown>>(`${this.apiUrl}/categorias`, { params })),
      timeline: optional(this.http.get<Record<string, unknown>>(`${this.apiUrl}/series-temporais`, { params })),
      neighborhoods: optional(this.http.get<Array<Record<string, unknown>>>(`${this.apiUrl}/mapa/bairros`, { params })),
      projectLifecycle: optional(this.http.get<Record<string, unknown>>(`${this.apiUrl}/projetos/ciclo-vida`, { params }))
    });
  }
}