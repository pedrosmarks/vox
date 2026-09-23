import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Subscription {
  id: number;
  userId: number;
  type: string;
  targetId: number | null;
}

@Injectable({ providedIn: 'root' })
export class SubscriptionService {
  private readonly API_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getSubscriptions(): Observable<Subscription[]> {
    return this.http.get<Subscription[]>(`${this.API_URL}/api/subscriptions`);
  }

  subscribeAllProjects(): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/subscriptions/all-projects`, null);
  }

  unsubscribeAllProjects(): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/subscriptions/all-projects`);
  }

  subscribeAllIssues(): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/subscriptions/all-issues`, null);
  }

  unsubscribeAllIssues(): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/subscriptions/all-issues`);
  }

  subscribeProject(projectId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/subscriptions/projects/${projectId}`, null);
  }

  unsubscribeProject(projectId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/subscriptions/projects/${projectId}`);
  }

  subscribeIssue(issueId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/subscriptions/issues/${issueId}`, null);
  }

  unsubscribeIssue(issueId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/subscriptions/issues/${issueId}`);
  }

  subscribeCategory(categoryId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/subscriptions/categories/${categoryId}`, null);
  }

  unsubscribeCategory(categoryId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/subscriptions/categories/${categoryId}`);
  }

  subscribeCouncilor(councilorId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/subscriptions/councilors/${councilorId}`, null);
  }

  unsubscribeCouncilor(councilorId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/api/subscriptions/councilors/${councilorId}`);
  }
}
