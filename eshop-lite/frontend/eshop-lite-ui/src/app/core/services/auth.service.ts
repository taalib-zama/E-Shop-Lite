import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap, switchMap } from 'rxjs/operators';
import { JwtResponse, LoginRequest, RegisterRequest, UserResponse } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API = 'http://localhost:8081';
  private readonly TOKEN_KEY = 'access_token';

  private _user = signal<UserResponse | null>(null);
  readonly user = this._user.asReadonly();
  readonly isLoggedIn = computed(() => !!this._user());

  constructor(private http: HttpClient, private router: Router) {
    const token = this.getToken();
    if (token) this.fetchProfile().subscribe();
  }

  register(req: RegisterRequest) {
    return this.http.post<UserResponse>(`${this.API}/users`, req);
  }

  login(req: LoginRequest) {
    return this.http.post<JwtResponse>(`${this.API}/auth/login`, req).pipe(
      tap(res => localStorage.setItem(this.TOKEN_KEY, res.accessToken)),
      switchMap(() => this.fetchProfile())
    );
  }

  fetchProfile() {
    return this.http.get<UserResponse>(`${this.API}/users/me`).pipe(
      tap(user => this._user.set(user))
    );
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    this._user.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
}
