import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

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
}

export type UserRole = 'ADMINISTRATOR' | 'MODERATOR' | 'CITIZEN';

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

  fetchCurrentUser(): Observable<UserProfile> {
    const email = this.getEmailFromToken();
    const url = email
      ? `${this.API_URL}/api/user/email/${encodeURIComponent(email)}`
      : `${this.API_URL}/api/user/me`;

    return this.http.get<UserProfile>(url).pipe(
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
    return stored ? Number(stored) : null;
  }

  getMunicipalityId(): number {
    const stored = localStorage.getItem(this.MUNICIPALITY_ID_KEY);
    return stored ? Number(stored) : 1;
  }
}
