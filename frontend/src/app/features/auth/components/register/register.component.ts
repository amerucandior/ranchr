import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="register-wrapper">
      <div class="register-card">
        <div class="brand">
          <span class="brand-badge">&#128004; RANCHR</span>
          <h2>Create an Account</h2>
          <p class="brand-sub">Livestock Genetics & KVB Veterinary Dispatch</p>
        </div>

        @if (errorMessage()) {
          <div class="alert alert-danger">{{ errorMessage() }}</div>
        }

        <form [formGroup]="registerForm" (ngSubmit)="submitRegister()">
          <!-- Role Selector -->
          <div class="form-group">
            <label>Select Your Primary Role</label>
            <div class="role-grid">
              <label class="role-card" [class.selected]="registerForm.get('role')?.value === 'USER'">
                <input type="radio" formControlName="role" value="USER" class="sr-only" />
                <span class="role-icon">🧑‍🌾</span>
                <span class="role-title">Farmer / Breeder</span>
                <span class="role-desc">Manage herd heat alerts & order AI genetics</span>
              </label>

              <label class="role-card" [class.selected]="registerForm.get('role')?.value === 'VET'">
                <input type="radio" formControlName="role" value="VET" class="sr-only" />
                <span class="role-icon">👨‍⚕️</span>
                <span class="role-title">KVB Veterinary Officer</span>
                <span class="role-desc">Receive emergency heat calls & field dispatch</span>
              </label>
            </div>
          </div>

          <div class="form-group">
            <label for="username">Username</label>
            <input
              id="username"
              type="text"
              class="form-control"
              formControlName="username"
              placeholder="e.g. farmer_john" />
          </div>

          <div class="form-group">
            <label for="email">Email Address</label>
            <input
              id="email"
              type="email"
              class="form-control"
              formControlName="email"
              placeholder="name@example.com" />
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <input
              id="password"
              type="password"
              class="form-control"
              formControlName="password"
              placeholder="At least 6 characters" />
          </div>

          <button
            type="submit"
            class="btn-submit"
            [disabled]="isLoading() || registerForm.invalid">
            {{ isLoading() ? 'Creating Account...' : 'Register & Continue' }}
          </button>
        </form>

        <div class="auth-switch">
          <span>Already have an account? </span>
          <a routerLink="/login" class="switch-link">Sign In</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .register-wrapper {
      min-height: 80vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1.5rem 1rem;
    }
    .register-card {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 2rem;
      max-width: 480px;
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
        margin-bottom: 0.4rem;
      }
      .form-control {
        width: 100%;
        padding: 0.75rem;
        border: 1px solid #cbd5e1;
        border-radius: 8px;
        box-sizing: border-box;
      }
    }
    .role-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.75rem;
      margin-bottom: 0.5rem;
    }
    .role-card {
      border: 2px solid #e2e8f0;
      border-radius: 8px;
      padding: 0.75rem;
      cursor: pointer;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      transition: all 0.2s ease;
      &.selected {
        border-color: #15803d;
        background-color: #f0fdf4;
      }
      .role-icon {
        font-size: 1.5rem;
        margin-bottom: 0.25rem;
      }
      .role-title {
        font-weight: 700;
        font-size: 0.85rem;
        color: #0f172a;
      }
      .role-desc {
        font-size: 0.7rem;
        color: #64748b;
        margin-top: 0.25rem;
      }
    }
    .sr-only {
      position: absolute;
      width: 1px;
      height: 1px;
      padding: 0;
      margin: -1px;
      overflow: hidden;
      clip: rect(0, 0, 0, 0);
      border: 0;
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
    .auth-switch {
      margin-top: 1.5rem;
      text-align: center;
      font-size: 0.85rem;
      color: #64748b;
      .switch-link {
        color: #15803d;
        font-weight: 700;
        text-decoration: none;
        &:hover {
          text-decoration: underline;
        }
      }
    }
  `],
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly isLoading = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);

  registerForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    role: ['USER' as 'USER' | 'VET', Validators.required],
  });

  submitRegister(): void {
    if (this.registerForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { username, email, password, role } = this.registerForm.value;

    this.authService
      .register({
        username: username!,
        email: email!,
        password: password!,
        role: role as 'USER' | 'VET',
      })
      .subscribe({
        next: () => {
          this.isLoading.set(false);
          this.router.navigate(['/kyc']);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error?.message || 'Registration failed. Username or email may already be taken.');
        },
      });
  }
}
