import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly API_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) { }

  getProjects(status?: string): Observable<Project[]> {
    const url = status
      ? `${this.API_URL}/api/project?status=${status}`
      : `${this.API_URL}/api/project`;
    return this.http.get<Project[]>(url);
  }

  getProjectById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.API_URL}/api/project/${id}`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.API_URL}/api/category`);
  }

  getCategoryById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.API_URL}/api/category/${id}`);
  }

  getProjectImages(projectId: number): Observable<ProjectImage[]> {
    return this.http.get<ProjectImage[]>(`${this.API_URL}/api/project-image/project-id/${projectId}`);
  }

  getUserById(id: number): Observable<UserSummary> {
    return this.http.get<UserSummary>(`${this.API_URL}/api/user/${id}`);
  }

  approveProject(id: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/api/moderation/projects/${id}/approve`, null);
  }

  rejectProject(id: number): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/api/project/${id}/reject`, null);
  }

  createProject(formData: FormData): Observable<Project> {
    return this.http.post<Project>(`${this.API_URL}/api/project`, formData);
  }

  updateProject(id: number, formData: FormData): Observable<Project> {
    return this.http.put<Project>(`${this.API_URL}/api/project/${id}`, formData);
  }
}
