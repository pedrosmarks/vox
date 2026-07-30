import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IssueReport {
  id: number;
  title: string;
  description: string;
  municipalityId: number;
  categoryId: number;
  status: string;
  authorId: number;
  createdAt: string;
  updatedAt: string;
  neighborhood: string;
  street: string;
  number: string;
  latitude: number;
  longitude: number;
}

export interface IssueImage {
  id: number;
  issueId: number;
  url: string;
}

export interface IssueStatusHistory {
  id: number;
  issueId: number;
  status: string;
  changedAt: string;
}

@Injectable({ providedIn: 'root' })
export class IssueService {
  private readonly API_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getIssues(page?: number, size?: number): Observable<IssueReport[]> {
    const params: string[] = [];
    if (page !== undefined) params.push(`page=${page}`);
    if (size !== undefined) params.push(`size=${size}`);
    const query = params.length ? `?${params.join('&')}` : '';
    return this.http.get<IssueReport[]>(`${this.API_URL}/api/issues${query}`);
  }

  getIssueById(id: number): Observable<IssueReport> {
    return this.http.get<IssueReport>(`${this.API_URL}/api/issues/${id}`);
  }

  getMyIssues(): Observable<IssueReport[]> {
    return this.http.get<IssueReport[]>(`${this.API_URL}/api/issues/my`);
  }

  createIssue(formData: FormData): Observable<IssueReport> {
    return this.http.post<IssueReport>(`${this.API_URL}/api/issues`, formData);
  }

  updateIssue(id: number, formData: FormData): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/api/issues/${id}`, formData);
  }

  deleteIssue(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/issues/${id}`);
  }

  getIssueHistory(id: number): Observable<IssueStatusHistory[]> {
    return this.http.get<IssueStatusHistory[]>(`${this.API_URL}/api/issues/${id}/history`);
  }

  getIssueImages(id: number): Observable<IssueImage[]> {
    return this.http.get<IssueImage[]>(`${this.API_URL}/api/issues/${id}/images`);
  }

  addIssueImage(id: number, formData: FormData): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/issues/${id}/images`, formData);
  }

  deleteIssueImage(id: number, imageId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/issues/${id}/images/${imageId}`);
  }

  getPendingIssues(page?: number, size?: number): Observable<IssueReport[]> {
    const params: string[] = [];
    if (page !== undefined) params.push(`page=${page}`);
    if (size !== undefined) params.push(`size=${size}`);
    const query = params.length ? `?${params.join('&')}` : '';
    return this.http.get<IssueReport[]>(`${this.API_URL}/api/moderation/issues/pending${query}`);
  }

  approveIssue(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/moderation/issues/${id}/approve`, null);
  }

  rejectIssue(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/moderation/issues/${id}/reject`, null);
  }
}
