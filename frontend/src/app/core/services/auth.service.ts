import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  AuthResponse,
  CurrentUserSession,
  DecodedJwtPayload,
  LoginRequest,
  RefreshRequest,
  RegisterRequest,
} from '../models/auth.models';

const ACCESS_TOKEN_KEY = 'ranchr_access_token';
const REFRESH_TOKEN_KEY = 'ranchr_refresh_token';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);

  readonly currentUser = signal<CurrentUserSession | null>(this.loadStoredUser());
  readonly isAuthenticated = computed(() => !!this.currentUser());
  readonly isVet = computed(() => this.currentUser()?.isVet ?? false);
  readonly isFarmer = computed(() => this.currentUser()?.isFarmer ?? false);

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>('/auth/login', credentials)
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>('/auth/register', payload)
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  refreshToken(): Observable<AuthResponse> {
    const refresh = this.getRefreshToken();
    const payload: RefreshRequest = { refreshToken: refresh ?? '' };
    return this.http
      .post<AuthResponse>('/auth/refresh', payload)
      .pipe(tap((res) => this.handleAuthResponse(res)));
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this.currentUser.set(null);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  private handleAuthResponse(res: AuthResponse): void {
    if (res.accessToken) {
      localStorage.setItem(ACCESS_TOKEN_KEY, res.accessToken);
    }
    if (res.refreshToken) {
      localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
    }
    const session = this.decodeSession(res.accessToken);
    this.currentUser.set(session);
  }

  private loadStoredUser(): CurrentUserSession | null {
    const token = this.getAccessToken();
    if (!token) {
      return null;
    }
    try {
      const decoded = this.parseJwt(token);
      if (decoded.exp && decoded.exp * 1000 < Date.now()) {
        this.logout();
        return null;
      }
      return this.decodeSession(token);
    } catch {
      this.logout();
      return null;
    }
  }

  private decodeSession(token: string): CurrentUserSession | null {
    try {
      const decoded = this.parseJwt(token);
      const roles = Array.isArray(decoded.roles) ? decoded.roles : [];
      const isVet = roles.includes('ROLE_VET') || roles.includes('VET');
      const isFarmer = roles.includes('ROLE_USER') || roles.includes('USER') || !isVet;
      return {
        username: decoded.sub,
        roles,
        isVet,
        isFarmer,
      };
    } catch {
      return null;
    }
  }

  private parseJwt(token: string): DecodedJwtPayload {
    const parts = token.split('.');
    if (parts.length !== 3) {
      throw new Error('Invalid JWT format');
    }
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload) as DecodedJwtPayload;
  }
}
