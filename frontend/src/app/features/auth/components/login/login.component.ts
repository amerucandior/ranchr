import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="login-wrapper">
      <div class="login-card">
        <div class="brand">
          <span class="brand-badge">&#128004; RANCHR</span>
          <h2>Sign In to Ranchr</h2>
          <p class="brand-sub">Livestock Genetics & KVB Veterinary Dispatch</p>
        </div>

        @if (errorMessage()) {
          <div class="alert alert-danger">{{ errorMessage() }}</div>
        }

        <form [formGroup]="loginForm" (ngSubmit)="submitLogin()">
          <div class="form-group">
            <label for="username">Username</label>
            <input
              id="username"
              type="text"
              class="form-control"
              formControlName="username"
              placeholder="e.g., farmer_john or dr_kamau" />
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <input
              id="password"
              type="password"
              class="form-control"
              formControlName="password"
              placeholder="••••••••" />
          </div>

          <button
            type="submit"
            class="btn-submit"
            [disabled]="isLoading() || loginForm.invalid">
            {{ isLoading() ? 'Authenticating...' : 'Sign In' }}
          </button>
        </form>

        <div class="quick-credentials">
          <span class="quick-title">Quick Demo Logins:</span>
          <div class="btn-group">
            <button
              type="button"
              class="btn-quick"
              (click)="fillCredentials('farmer_demo', 'password123')">
              🧑‍🌾 Farmer Login
            </button>
            <button
              type="button"
              class="btn-quick"
              (click)="fillCredentials('vet_kamau', 'password123')">
              👨‍⚕️ KVB Vet Login
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      min-height: 80vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1rem;
    }
    .login-card {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 2rem;
      max-width: 400px;
      width: 100%;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    }
    .brand {
      text-align: center;
      margin-bottom: 1.5rem;
    }
    .brand-badge {
      font-weight: 800;
      letter-spacing: 0.1em;
      color: #15803d;
      font-size: 0.85rem;
    }
    h2 {
      margin: 0.3rem 0;
      font-size: 1.5rem;
      color: #0f172a;
    }
    .brand-sub {
      font-size: 0.8rem;
      color: #64748b;
      margin: 0;
    }
    .alert-danger {
      background: #fef2f2;
      border: 1px solid #f87171;
      color: #b91c1c;
      padding: 0.75rem;
      border-radius: 6px;
      font-size: 0.85rem;
      margin-bottom: 1rem;
    }
    .form-group {
      margin-bottom: 1rem;
      label {
        display: block;
        font-weight: 600;
        font-size: 0.85rem;
        color: #334155;
        margin-bottom: 0.3rem;
      }
      .form-control {
        width: 100%;
        padding: 0.75rem;
        border: 1px solid #cbd5e1;
        border-radius: 8px;
        box-sizing: border-box;
      }
    }
    .btn-submit {
      width: 100%;
      padding: 0.85rem;
      background: #15803d;
      color: #ffffff;
      border: none;
      border-radius: 8px;
      font-weight: 700;
      font-size: 0.95rem;
      cursor: pointer;
      margin-top: 0.5rem;
      &:hover:not(:disabled) {
        background: #166534;
      }
      &:disabled {
        opacity: 0.6;
      }
    }
    .quick-credentials {
      margin-top: 1.5rem;
      padding-top: 1rem;
      border-top: 1px solid #f1f5f9;
      .quick-title {
        display: block;
        font-size: 0.75rem;
        font-weight: 700;
        color: #94a3b8;
        margin-bottom: 0.5rem;
        text-transform: uppercase;
      }
      .btn-group {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 0.5rem;
        .btn-quick {
          padding: 0.5rem;
          background: #f8fafc;
          border: 1px solid #cbd5e1;
          border-radius: 6px;
          font-size: 0.8rem;
          font-weight: 600;
          cursor: pointer;
          &:hover {
            background: #f1f5f9;
          }
        }
      }
    }
  `],
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly isLoading = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);

  loginForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  fillCredentials(u: string, p: string): void {
    this.loginForm.setValue({ username: u, password: p });
  }

  submitLogin(): void {
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { username, password } = this.loginForm.value;
    this.authService.login({ username: username!, password: password! }).subscribe({
      next: () => {
        this.isLoading.set(false);
        if (this.authService.isVet()) {
          this.router.navigate(['/vet-console']);
        } else {
          this.router.navigate(['/dispatch']);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Invalid username or password.');
      },
    });
  }
}
