import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';

export interface LoginResponse {
  token: string;
}

export interface UserProfile {
  id: number;
  email: string;
  name: string;
  fullname?: string;
  role: string;
  municipalityId: number;
  phone?: string;
  cpf?: string;
  birthDate?: string;
  profilePhotoUrl?: string | null;
}

export type UserRole = 'ADMINISTRATOR' | 'MODERATOR' | 'CITIZEN' | 'COUNCILOR';

export interface LogEntry {
  id: number;
  userId: number | null;
  userRole: string | null;
  municipalityId: number | null;
  httpMethod: string;
  path: string;
  queryString: string | null;
  statusCode: number;
  success: boolean;
  durationMs: number;
  ipAddress: string | null;
  userAgent: string | null;
  errorMessage: string | null;
  createdAt: string;
}

export interface LogPage {
  content: LogEntry[];
  page: number;
  size: number;
  totalElements: number;
}

export interface LogFilters {
  page: number;
  size: number;
  userId?: number;
  method?: string;
  from?: string;
  to?: string;
}

export interface RegisterPayload {
  name: string;
  email: string;
  cpf: string;
  phone?: string;
  password: string;
  birthDate?: string;
  municipalityId: number;
  acceptedTerms?: boolean;
  acceptedPrivacyPolicy?: boolean;
  file?: File | null;
}

export interface CreateUserPayload {
  name: string;
  email: string;
  cpf: string;
  phone?: string;
  password: string;
  birthDate?: string;
  role: UserRole | string;
  municipalityId: number;
  acceptedTerms?: boolean;
  acceptedPrivacyPolicy?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = 'http://localhost:8080';
  private readonly TOKEN_KEY = 'token';
  private readonly USER_ID_KEY = 'userId';
  private readonly MUNICIPALITY_ID_KEY = 'municipalityId';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/authenticate`, { email, password }).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
      })
    );
  }

  /**
   * Cadastro público de novo cidadão. O papel é sempre CITIZEN (não é
   * escolhido pelo usuário). Retorna a resposta do POST /api/user.
   */
  register(payload: RegisterPayload): Observable<unknown> {
    const formData = new FormData();
    const fields = {
      name: payload.name,
      email: payload.email,
      cpf: payload.cpf,
      phone: payload.phone ?? '',
      password: payload.password,
      birthDate: payload.birthDate ?? null,
      role: 'CITIZEN',
      municipalityId: payload.municipalityId,
      acceptedTerms: payload.acceptedTerms ?? true,
      acceptedPrivacyPolicy: payload.acceptedPrivacyPolicy ?? true
    };
    Object.entries(fields).forEach(([key, value]) => formData.append(key, String(value)));
    if (payload.file) formData.append('file', payload.file, payload.file.name);
    return this.http.post(`${this.API_URL}/api/user`, formData);
  }

  fetchCurrentUser(): Observable<UserProfile> {
    return this.http.get<Record<string, unknown>>(`${this.API_URL}/api/auth/me`).pipe(
      map(data => ({
        ...data,
        profilePhotoUrl: this.normalizePhotoUrl(
          data['profilePhotoUrl'] ?? data['profilePhoto'] ?? data['photoUrl']
        )
      } as UserProfile)),
      tap(user => {
        if (user?.id) {
          localStorage.setItem(this.USER_ID_KEY, String(user.id));
        }
        if (user?.municipalityId) {
          localStorage.setItem(this.MUNICIPALITY_ID_KEY, String(user.municipalityId));
        }
      })
    );
  }

  private normalizePhotoUrl(value: unknown): string | null {
    if (typeof value !== 'string' || !value.trim()) return null;
    if (/^https?:\/\//i.test(value)) return value;
    return `${this.API_URL}/${value.replace(/^\/+/, '')}`;
  }

  private getEmailFromToken(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
      return decoded.email ?? decoded.sub ?? null;
    } catch {
      return null;
    }
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    localStorage.removeItem(this.MUNICIPALITY_ID_KEY);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.TOKEN_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUserRole(): UserRole | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
      return decoded.role ?? decoded.roles?.[0] ?? null;
    } catch {
      return null;
    }
  }

  getUserId(): number | null {
    const stored = localStorage.getItem(this.USER_ID_KEY);
    if (stored) return Number(stored);
    // fallback: ler do payload do JWT
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
      const id = decoded.userId ?? decoded.user_id ?? decoded.sub_id ?? null;
      return id != null ? Number(id) : null;
    } catch {
      return null;
    }
  }

  getMunicipalityId(): number {
    const stored = localStorage.getItem(this.MUNICIPALITY_ID_KEY);
    if (stored) return Number(stored);
    // fallback: ler do payload do JWT
    const token = this.getToken();
    if (!token) return 1;
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')));
      return decoded.municipalityId ?? decoded.municipality_id ?? 1;
    } catch {
      return 1;
    }
  }

  updateProfile(id: number, data: Partial<UserProfile>, file?: File | null): Observable<void> {
    const formData = new FormData();
    Object.entries(data).forEach(([key, value]) => {
      if (value !== undefined && value !== null) formData.append(key, String(value));
    });
    if (file) formData.append('file', file, file.name);
    return this.http.put<void>(`${this.API_URL}/api/user/${id}`, formData);
  }

  updatePassword(oldPassword: string, newPassword: string): Observable<void> {
    const id = this.getUserId();
    return this.http.put<void>(`${this.API_URL}/api/user/update-password`, {
      ...(id != null ? { id } : {}),
      oldPassword,
      newPassword
    });
  }

  forgotPassword(email: string): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/auth/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/auth/reset-password`, { token, newPassword });
  }

  getCouncilors(): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(`${this.API_URL}/api/users/councilors`);
  }

  getCouncilorById(id: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_URL}/api/users/councilors/${id}`);
  }

  // ----- Administração de usuários -----

  getAllUsers(): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(`${this.API_URL}/api/user`);
  }

  getUsersByRole(role: UserRole | string): Observable<UserProfile[]> {
    return this.http.get<UserProfile[]>(`${this.API_URL}/api/user/role/${role}`);
  }

  getUserById(id: number): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_URL}/api/user/${id}`);
  }

  createUser(payload: CreateUserPayload): Observable<unknown> {
    const formData = new FormData();
    Object.entries(payload).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        formData.append(key, String(value));
      }
    });
    return this.http.post(`${this.API_URL}/api/user`, formData);
  }

  updateUser(id: number, data: Partial<UserProfile>): Observable<void> {
    const formData = new FormData();
    Object.entries(data).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        formData.append(key, String(value));
      }
    });
    return this.http.put<void>(`${this.API_URL}/api/user/${id}`, formData);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/user/${id}`);
  }

  // ----- Auditoria / logs -----

  getLogs(filters: LogFilters): Observable<LogPage> {
    let params = new HttpParams()
      .set('page', filters.page)
      .set('size', filters.size);
    if (filters.userId != null) params = params.set('userId', filters.userId);
    if (filters.method) params = params.set('method', filters.method);
    if (filters.from) params = params.set('from', filters.from);
    if (filters.to) params = params.set('to', filters.to);
    return this.http.get<LogPage>(`${this.API_URL}/api/admin/logs`, { params });
  }
}
