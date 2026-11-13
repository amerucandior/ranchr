import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="login-wrapper">
      <div class="login-card">
        <!-- Brand & Heading -->
        <div class="brand">
          <span class="arva-tag">EAST AFRICAN BREEDING REGISTER</span>
          <h2 class="serif-title">Sign In to Ranchr</h2>
          <p class="brand-sub">Pastoral genetics & KVB veterinary field dispatch</p>
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
              placeholder="e.g. farmer_john or dr_kamau" />
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
            class="btn-arva-submit"
            [disabled]="isLoading() || loginForm.invalid">
            {{ isLoading() ? 'Authenticating...' : 'Sign In to Console →' }}
          </button>
        </form>

        <!-- Quick Demo Credentials (Arva Soft Capsules) -->
        <div class="quick-credentials">
          <span class="quick-title">Quick Demo Profiles:</span>
          <div class="btn-group">
            <button
              type="button"
              class="btn-quick-capsule"
              (click)="fillCredentials('farmer_demo', 'password123')">
              🧑‍🌾 Farmer Demo
            </button>
            <button
              type="button"
              class="btn-quick-capsule vet"
              (click)="fillCredentials('vet_kamau', 'password123')">
              👨‍⚕️ KVB Vet Demo
            </button>
          </div>
        </div>

        <div class="auth-switch">
          <span>Don't have an account? </span>
          <a routerLink="/register" class="switch-link">Create Account</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-wrapper {
      min-height: 85vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      background: var(--color-cream);
    }

    .login-card {
      background: #ffffff;
      border: 1px solid rgba(7, 80, 63, 0.12);
      border-radius: 30px;
      padding: 48px 40px;
      max-width: 440px;
      width: 100%;
      box-shadow: 0 16px 40px rgba(7, 80, 63, 0.05);

      @media (max-width: 480px) {
        padding: 32px 20px;
        border-radius: 20px;
      }
    }

    .brand {
      text-align: center;
      margin-bottom: 28px;

      .arva-tag {
        font-family: var(--font-sans);
        font-size: 11px;
        font-weight: 600;
        letter-spacing: 0.08em;
        color: var(--color-forest);
        background: var(--color-benefit-cream);
        padding: 4px 14px;
        border-radius: 110px;
        display: inline-block;
        margin-bottom: 12px;
      }

      .serif-title {
        font-family: var(--font-serif);
        font-weight: 300;
        font-size: 34px;
        line-height: 1.1;
        color: var(--color-forest);
        margin: 0 0 6px 0;
      }

      .brand-sub {
        font-family: var(--font-sans);
        font-size: 14px;
        color: var(--color-muted);
        margin: 0;
      }
    }

    .alert-danger {
      background: #fef2f2;
      border: 1px solid #fca5a5;
      color: #991b1b;
      padding: 12px 16px;
      border-radius: 10px;
      font-size: 13.5px;
      margin-bottom: 20px;
    }

    .form-group {
      margin-bottom: 18px;

      label {
        display: block;
        font-family: var(--font-sans);
        font-weight: 500;
        font-size: 13.5px;
        color: var(--color-deep-ink);
        margin-bottom: 6px;
      }

      .form-control {
        width: 100%;
        padding: 12px 16px;
        background: var(--color-mist);
        border: 1px solid rgba(0, 0, 0, 0.1);
        border-radius: 10px;
        font-family: var(--font-sans);
        font-size: 14.5px;
        color: var(--color-ink);
        outline: none;
        transition: border-color 0.2s, background 0.2s;

        &:focus {
          border-color: var(--color-forest);
          background: #ffffff;
        }
      }
    }

    .btn-arva-submit {
      width: 100%;
      height: 49px;
      background: var(--color-forest);
      color: #ffffff;
      border: none;
      border-radius: 110px;
      font-family: var(--font-sans);
      font-weight: 500;
      font-size: 15px;
      letter-spacing: 0.02em;
      cursor: pointer;
      margin-top: 10px;
      transition: all 0.3s ease;

      &:hover:not(:disabled) {
        background: #053b2e;
        box-shadow: 0 4px 14px rgba(7, 80, 63, 0.2);
      }

      &:disabled {
        opacity: 0.55;
        cursor: not-allowed;
      }
    }

    .quick-credentials {
      margin-top: 28px;
      padding-top: 20px;
      border-top: 1px solid rgba(0, 0, 0, 0.08);

      .quick-title {
        display: block;
        font-size: 11.5px;
        font-weight: 600;
        letter-spacing: 0.05em;
        color: var(--color-muted);
        margin-bottom: 10px;
        text-align: center;
      }

      .btn-group {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 8px;

        .btn-quick-capsule {
          background: var(--color-benefit-cream);
          border: 1px solid rgba(7, 80, 63, 0.15);
          color: var(--color-forest);
          padding: 8px 12px;
          border-radius: 110px;
          font-family: var(--font-sans);
          font-size: 12.5px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            background: var(--color-lime);
          }

          &.vet {
            background: rgba(178, 206, 231, 0.25);
            border-color: var(--color-sky);
            color: var(--color-forest);

            &:hover {
              background: var(--color-sky);
            }
          }
        }
      }
    }

    .auth-switch {
      margin-top: 24px;
      text-align: center;
      font-size: 13.5px;
      color: var(--color-muted);

      .switch-link {
        color: var(--color-forest);
        font-weight: 600;
        text-decoration: none;

        &:hover {
          text-decoration: underline;
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
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  fillCredentials(username: string, password: string): void {
    this.loginForm.patchValue({ username, password });
  }

  submitLogin(): void {
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { username, password } = this.loginForm.value;

    this.authService
      .login({
        username: username!,
        password: password!,
      })
      .subscribe({
        next: () => {
          this.isLoading.set(false);
          const target = this.authService.isVet() ? '/vet-console' : '/dispatch';
          this.router.navigate([target]);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error?.message || 'Invalid username or password.');
        },
      });
  }
}
