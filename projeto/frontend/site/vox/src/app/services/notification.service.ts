import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Notification {
  id: number;
  message: string;
  read: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly API_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.API_URL}/api/notifications`);
  }

  getUnreadNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.API_URL}/api/notifications/unread`);
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.API_URL}/api/notifications/count`);
  }

  markAsRead(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/api/notifications/${id}/read`, null);
  }

  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/api/notifications/read-all`, null);
  }
}
