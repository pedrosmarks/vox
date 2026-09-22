import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, tap } from 'rxjs';
import { catchError } from 'rxjs/operators';

export type AccessibilityMode =
  | 'NONE'
  | 'DARK'
  | 'HIGH_CONTRAST'
  | 'PROTANOPIA'
  | 'DEUTERANOPIA'
  | 'TRITANOPIA';

export interface UserSettings {
  id?: number;
  userId?: number;
  fontSize: number; // 15–30
  accessibilityMode: AccessibilityMode;
}

const DEFAULT_SETTINGS: UserSettings = {
  fontSize: 16,
  accessibilityMode: 'NONE'
};

@Injectable({ providedIn: 'root' })
export class SettingsService {
  private readonly API_URL = 'http://localhost:8080';

  /** Estado atual, observável pelos componentes. */
  private readonly _settings = new BehaviorSubject<UserSettings>({ ...DEFAULT_SETTINGS });
  readonly settings$ = this._settings.asObservable();

  constructor(private http: HttpClient) {}

  get current(): UserSettings {
    return this._settings.value;
  }

  /** Busca as configurações do backend e aplica na interface. */
  load(): Observable<UserSettings> {
    return this.http.get<UserSettings>(`${this.API_URL}/api/settings`).pipe(
      catchError(() => of({ ...DEFAULT_SETTINGS })),
      tap(settings => {
        const normalized = this.normalize(settings);
        this._settings.next(normalized);
        this.apply(normalized);
      })
    );
  }

  /** Salva no backend e aplica imediatamente na interface. */
  save(settings: UserSettings): Observable<void> {
    const normalized = this.normalize(settings);
    // Aplica na hora (otimista) para feedback imediato.
    this._settings.next(normalized);
    this.apply(normalized);
    return this.http.put<void>(`${this.API_URL}/api/settings`, {
      fontSize: normalized.fontSize,
      accessibilityMode: normalized.accessibilityMode
    });
  }

  /** Restaura os valores padrão em memória (usado no logout). */
  reset(): void {
    this._settings.next({ ...DEFAULT_SETTINGS });
    this.apply(DEFAULT_SETTINGS);
  }

  private normalize(settings: UserSettings | null): UserSettings {
    const fontSize = Math.min(30, Math.max(15, Math.round(settings?.fontSize ?? 16)));
    const mode = (settings?.accessibilityMode ?? 'NONE') as AccessibilityMode;
    return { ...settings, fontSize, accessibilityMode: mode };
  }

  /**
   * Aplica as configurações à interface inteira:
   * - escala de fonte via CSS custom property em <html>;
   * - modo de acessibilidade via atributo data-a11y (dirige o CSS global).
   */
  private apply(settings: UserSettings): void {
    const root = document.documentElement;
    // 16px é a base; a escala é relativa a isso.
    const scale = settings.fontSize / 16;
    root.style.setProperty('--vox-font-scale', String(scale));
    root.style.fontSize = `${settings.fontSize}px`;
    root.setAttribute('data-a11y', settings.accessibilityMode);
  }
}
