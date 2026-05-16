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
  highlighted: boolean;
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

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly API_URL = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${this.API_URL}/api/project`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.API_URL}/api/category`);
  }

  createProject(formData: FormData): Observable<Project> {
    return this.http.post<Project>(`${this.API_URL}/api/project`, formData);
  }
}
