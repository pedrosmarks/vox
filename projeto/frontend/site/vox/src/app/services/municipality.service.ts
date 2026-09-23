import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Municipality {
  id: number;
  name: string;
  state?: string;
}

@Injectable({ providedIn: 'root' })
export class MunicipalityService {
  private readonly API_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getMunicipalities(): Observable<Municipality[]> {
    return this.http.get<Municipality[]>(`${this.API_URL}/api/municipality`);
  }

  getMunicipalityById(id: number): Observable<Municipality> {
    return this.http.get<Municipality>(`${this.API_URL}/api/municipality/${id}`);
  }

  createMunicipality(data: Partial<Municipality>): Observable<Municipality> {
    return this.http.post<Municipality>(`${this.API_URL}/api/municipality`, data);
  }
}
