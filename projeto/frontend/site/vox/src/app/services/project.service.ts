import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface Project {
  id: number;
  municipalityId: number;
  categoryId: number;
  type: string;
  title: string;
  description: string;
  status: string;
  authorId: number;
  createdAt: string;
  updatedAt: string;
  highlighted: boolean;
  isOfficial: boolean;
  neighborhood: string;
  street: string;
  number: string;
  latitude: number;
  longitude: number;
  startDate: string;
  expectedEndDate: string;
  endDate: string | null;
  financialAnalysis: string | null;
  estimatedCost: number;
  approvedBudget: number;
  // Comentário do moderador (ex.: motivo da rejeição). O nome do campo pode
  // variar conforme o backend, então lemos de várias chaves possíveis.
  feedback?: string | null;
  moderationFeedback?: string | null;
  rejectionReason?: string | null;
  note?: string | null;
}

/** Status de projeto aceitos pelo backend (ver API.md). */
export const PROJECT_STATUSES = [
  'PENDING_APPROVAL',
  'REJECTED',
  'PUBLISHED',
  'IN_VOTING',
  'SELECTED_BY_COUNCIL',
  'APPROVED_BY_COUNCIL',
  'IN_EXECUTION',
  'COMPLETED',
  'ARCHIVED',
  'CANCELLED'
] as const;

export type ProjectStatus = (typeof PROJECT_STATUSES)[number];

/**
 * Extrai o comentário de rejeição/cancelamento mais recente do histórico.
 * Procura a última entrada cujo novo status é REJECTED ou CANCELLED e devolve
 * o `note` associado.
 */
export function latestRejectionNote(history: ProjectHistoryEntry[]): string {
  const rejections = history
    .filter(h => h.newStatus === 'REJECTED' || h.newStatus === 'CANCELLED')
    .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1));
  return rejections[0]?.note?.trim() ?? '';
}

export interface Category {
  id: number;
  name: string;
}

export interface UserSummary {
  id: number;
  name: string;
  fullname?: string;
}

export interface ProjectImage {
  id: number;
  projectId: number;
  url: string;
}

/** Estatísticas de opiniões de um projeto (formato real do backend). */
export interface OpinionStats {
  approved: number;
  disapproved: number;
  neutral: number;
  total: number;
}

/** Opinião do usuário atual em um projeto. */
export interface MyOpinion {
  id: number;
  projectId: number;
  userId: number;
  opinion: 'APPROVE' | 'DISAPPROVE' | 'NEUTRAL';
  createdAt: string;
  updatedAt: string;
}

/** Entrada do histórico de mudança de status de um projeto. */
export interface ProjectHistoryEntry {
  id: number;
  projectId: number;
  previousStatus: string;
  newStatus: string;
  note: string;
  changedBy: number;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly API_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getProjects(status?: string): Observable<Project[]> {
    const url = status
      ? `${this.API_URL}/api/project?status=${status}`
      : `${this.API_URL}/api/project`;
    return this.http.get<Project[]>(url);
  }

  getProjectById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.API_URL}/api/project/${id}`);
  }

  /** Histórico de mudanças de status do projeto (contém o comentário/note). */
  getProjectHistory(id: number): Observable<ProjectHistoryEntry[]> {
    return this.http.get<ProjectHistoryEntry[]>(`${this.API_URL}/api/project/${id}/history`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.API_URL}/api/categories`);
  }

  getCategoryById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.API_URL}/api/categories/${id}`);
  }

  getProjectImages(projectId: number): Observable<ProjectImage[]> {
    return this.http.get<ProjectImage[]>(`${this.API_URL}/api/project/${projectId}/image`);
  }

  getUserById(id: number): Observable<UserSummary> {
    return this.http.get<UserSummary>(`${this.API_URL}/api/user/${id}`);
  }

  approveProject(id: number, feedback?: string): Observable<void> {
    const body = feedback?.trim() ? { feedback: feedback.trim() } : null;
    // responseType 'text' evita erro de parse quando o backend responde 200/204 sem JSON.
    return this.http.post(`${this.API_URL}/api/moderation/projects/${id}/approve`, body, {
      responseType: 'text'
    }) as unknown as Observable<void>;
  }

  /**
   * Rejeita o projeto com um comentário visível ao cidadão.
   * Usa PATCH /status porque é o endpoint que persiste o `note` no histórico
   * (o /reject não registra o comentário de forma recuperável).
   */
  rejectProject(id: number, feedback?: string): Observable<void> {
    return this.updateProjectStatus(id, 'REJECTED', feedback);
  }

  /**
   * Atualiza o status do projeto (moderador/admin).
   * `note` é um comentário opcional associado à mudança de status.
   */
  updateProjectStatus(id: number, status: string, note?: string): Observable<void> {
    const body: { status: string; note?: string } = { status };
    if (note?.trim()) body.note = note.trim();
    // responseType 'text' evita erro de parse do Angular quando o backend
    // responde 204 No Content (corpo vazio) — causa comum de "falha" aparente.
    return this.http.patch(`${this.API_URL}/api/moderation/projects/${id}/status`, body, {
      responseType: 'text'
    }) as unknown as Observable<void>;
  }

  getPendingProjects(page?: number, size?: number): Observable<Project[]> {
    const params: string[] = [];
    if (page !== undefined) params.push(`page=${page}`);
    if (size !== undefined) params.push(`size=${size}`);
    const query = params.length ? `?${params.join('&')}` : '';
    return this.http.get<Project[]>(`${this.API_URL}/api/moderation/projects/pending${query}`).pipe(
      map(projects => projects.filter(project => project.status === 'PENDING_APPROVAL'))
    );
  }

  createProject(formData: FormData): Observable<Project> {
    return this.http.post<Project>(`${this.API_URL}/api/project`, formData);
  }

  updateProject(id: number, formData: FormData): Observable<Project> {
    return this.http.put<Project>(`${this.API_URL}/api/project/${id}`, formData);
  }

  // ----- Assinaturas de apoio ao projeto (petição) -----

  /** Assina (apoia) o projeto. */
  signProject(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/project/${id}/signature`, null);
  }

  /** Remove a assinatura de apoio do projeto. */
  unsignProject(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/project/${id}/signature`);
  }

  /** Verifica se o usuário atual já assinou o projeto. */
  hasSignedProject(id: number): Observable<{ signed: boolean }> {
    return this.http.get<{ signed: boolean }>(`${this.API_URL}/api/project/${id}/signature/me`);
  }

  /** Contagem total de assinaturas do projeto. */
  getSignatureCount(id: number): Observable<{ total: number }> {
    return this.http.get<{ total: number }>(`${this.API_URL}/api/project/${id}/signature/count`);
  }

  // ----- Opiniões / Apoios (opinion APPROVE) -----

  /** Registra a opinião do usuário. APPROVE = apoiar; NEUTRAL = remover apoio. */
  setOpinion(id: number, opinion: 'APPROVE' | 'DISAPPROVE' | 'NEUTRAL'): Observable<void> {
    // responseType 'text' evita erro de parse quando o backend responde sem JSON.
    return this.http.post(`${this.API_URL}/api/project/${id}/opinion`, { opinion }, {
      responseType: 'text'
    }) as unknown as Observable<void>;
  }

  /** Estatísticas de opiniões. O backend retorna approved/disapproved/neutral/total. */
  getOpinionStats(id: number): Observable<OpinionStats> {
    return this.http.get<OpinionStats>(`${this.API_URL}/api/project/${id}/opinion/stats`);
  }

  /**
   * Opinião do usuário atual no projeto. Retorna null quando o usuário ainda
   * não opinou (backend responde 404 nesse caso).
   */
  getMyOpinion(id: number): Observable<MyOpinion | null> {
    return this.http.get<MyOpinion>(`${this.API_URL}/api/project/${id}/opinion/me`).pipe(
      catchError(() => of(null))
    );
  }
}
