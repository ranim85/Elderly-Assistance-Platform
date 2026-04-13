import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  id?: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  linkedElderlyPersonId?: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);

  getCurrentUser(): Observable<User> {
    return this.http.get<User>('/api/users/me');
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }

  createUser(user: Record<string, unknown>): Observable<User> {
    return this.http.post<User>('/api/users', user);
  }

  updateUser(id: number, user: Record<string, unknown>): Observable<User> {
    return this.http.put<User>(`/api/users/${id}`, user);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`/api/users/${id}`);
  }
}
