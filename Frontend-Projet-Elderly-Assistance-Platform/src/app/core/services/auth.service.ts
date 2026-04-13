import { Injectable, inject } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'jwt_token';
  private readonly ROLE_KEY = 'user_role';
  private readonly REFRESH_KEY = 'jwt_refresh_token';

  private router = inject(Router);

  // Reactive state management using BehaviorSubject
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private userRoleSubject = new BehaviorSubject<string | null>(this.getRole());
  public userRole$ = this.userRoleSubject.asObservable();

  constructor() {}

  public saveToken(token: string, role?: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    if (role) {
      localStorage.setItem(this.ROLE_KEY, role);
      this.userRoleSubject.next(role);
    }
    this.isAuthenticatedSubject.next(true);
  }

  public saveRefreshToken(token: string): void {
    localStorage.setItem(this.REFRESH_KEY, token);
  }

  public getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_KEY);
  }

  public getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  public getRole(): string | null {
    return localStorage.getItem(this.ROLE_KEY);
  }
  
  public hasRole(role: string): boolean {
    return this.userRoleSubject.value === role;
  }

  public logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ROLE_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    
    // Update state synchronously
    this.isAuthenticatedSubject.next(false);
    this.userRoleSubject.next(null);
    
    // Attempt navigation immediately
    this.router.navigate(['/login']);
  }

  public hasToken(): boolean {
    return !!this.getToken();
  }
}
