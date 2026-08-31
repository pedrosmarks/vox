import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, of, switchMap } from 'rxjs';

export type SalaStatus = 'OPEN' | 'CLOSED';
export type SolicitacaoStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REMOVED';

export interface Sala {
  id: number;
  name: string;
  description: string;
  moderatorId: number;
  municipalityId: number;
  status: SalaStatus;
  createdAt: string;
}

export interface SolicitacaoEntrada {
  id: number;
  roomId: number;
  userId: number;
  status: SolicitacaoStatus;
  canPublishAudio: boolean;
  canPublishVideo: boolean;
  requestedAt: string;
}

export interface CreateSalaPayload {
  name: string;
  description: string;
}

@Injectable({ providedIn: 'root' })
export class SalaService {
  private readonly API_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  createSala(payload: CreateSalaPayload): Observable<Sala> {
    // Backend responde 201 só com header Location (sem corpo), igual /api/project.
    // No navegador o header Location pode não estar exposto via CORS
    // (Access-Control-Expose-Headers), então cai para recarregar a lista.
    return this.http.post(`${this.API_URL}/api/salas`, payload, { observe: 'response' }).pipe(
      switchMap(res => {
        if (res.body) return of(res.body as Sala);
        const location = res.headers.get('Location') || '';
        const id = Number(location.split('/').filter(Boolean).pop());
        if (!isNaN(id) && id > 0) return this.getSalaById(id);
        return this.getSalas().pipe(
          map(salas => {
            const matches = salas.filter(
              s => s.name === payload.name && s.description === payload.description
            );
            return matches.reduce((a, b) => (a.id > b.id ? a : b), matches[0] ?? salas[0]);
          })
        );
      })
    );
  }

  getSalas(): Observable<Sala[]> {
    return this.http.get<Sala[]>(`${this.API_URL}/api/salas`);
  }

  getSalaById(id: number): Observable<Sala> {
    return this.http.get<Sala>(`${this.API_URL}/api/salas/${id}`);
  }

  encerrarSala(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/salas/${id}`);
  }

  solicitarEntrada(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/salas/${id}/solicitacoes-entrada`, null);
  }

  getSolicitacoesEntrada(id: number): Observable<SolicitacaoEntrada[]> {
    return this.http.get<SolicitacaoEntrada[]>(`${this.API_URL}/api/salas/${id}/solicitacoes-entrada`);
  }

  aprovarSolicitacao(id: number, participanteId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/salas/${id}/solicitacoes-entrada/${participanteId}/aprovar`, null);
  }

  rejeitarSolicitacao(id: number, participanteId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/salas/${id}/solicitacoes-entrada/${participanteId}/rejeitar`, null);
  }

  liberarMicrofone(id: number, participanteId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/salas/${id}/participantes/${participanteId}/microfone/liberar`, null);
  }

  bloquearMicrofone(id: number, participanteId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/salas/${id}/participantes/${participanteId}/microfone/bloquear`, null);
  }

  liberarCamera(id: number, participanteId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/salas/${id}/participantes/${participanteId}/camera/liberar`, null);
  }

  bloquearCamera(id: number, participanteId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/salas/${id}/participantes/${participanteId}/camera/bloquear`, null);
  }

  expulsarParticipante(id: number, participanteId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/salas/${id}/participantes/${participanteId}`);
  }

  gerarToken(id: number): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.API_URL}/api/salas/${id}/token`, null);
  }
}
