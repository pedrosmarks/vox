import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EventCategory { id: number; name: string; description?: string; }
export interface EventImage { id: number; eventId?: number; url: string; }
export interface CivicEvent {
  id: number;
  title: string;
  description?: string;
  categoryId: number;
  price?: number | null;
  startDate?: string | null;
  endDate?: string | null;
  neighborhood?: string | null;
  street?: string | null;
  number?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  /** Legacy payload compatibility. */
  location?: string | null;
  municipalityId?: number;
  authorId?: number;
  createdAt?: string;
  images?: EventImage[];
}
export interface EventPage { content: CivicEvent[]; page: number; size: number; totalElements: number; }
export interface EventFilters {
  search?: string; categoryId?: number; municipalityId?: number; free?: boolean; hasImage?: boolean;
  startFrom?: string; startTo?: string; page?: number; size?: number;
}

@Injectable({ providedIn: 'root' })
export class EventService {
  private readonly apiUrl = 'http://localhost:8080/api';
  constructor(private http: HttpClient) {}

  getCategories(): Observable<EventCategory[]> {
    return this.http.get<EventCategory[]>(`${this.apiUrl}/event-categories`);
  }

  getEvents(filters: EventFilters = {}): Observable<EventPage> {
    let params = new HttpParams();
    Object.entries({ size: 12, page: 0, ...filters }).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') params = params.set(key, String(value));
    });
    return this.http.get<EventPage>(`${this.apiUrl}/events`, { params });
  }

  getEvent(id: number): Observable<CivicEvent> {
    return this.http.get<CivicEvent>(`${this.apiUrl}/events/${id}`);
  }

  getEventImages(id: number): Observable<EventImage[]> {
    return this.http.get<EventImage[]>(`${this.apiUrl}/events/${id}/images`);
  }

  createEvent(data: Record<string, string>, file?: File): Observable<void> {
    return this.http.post(`${this.apiUrl}/events`, this.formData(data, file), { responseType: 'text' }) as unknown as Observable<void>;
  }

  updateEvent(id: number, data: Record<string, string>, file?: File): Observable<void> {
    return this.http.put(`${this.apiUrl}/events/${id}`, this.formData(data, file), { responseType: 'text' }) as unknown as Observable<void>;
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete(`${this.apiUrl}/events/${id}`, { responseType: 'text' }) as unknown as Observable<void>;
  }

  private formData(data: Record<string, string>, file?: File): FormData {
    const form = new FormData();
    Object.entries(data).forEach(([key, value]) => { if (value !== '') form.append(key, value); });
    if (file) form.append('file', file, file.name);
    return form;
  }
}
