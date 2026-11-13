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
        <!-- Brand & Heading -->
        <div class="brand">
          <span class="arva-tag">EAST AFRICAN BREEDING NETWORK</span>
          <h2 class="serif-title">Create an Account</h2>
          <p class="brand-sub">Pastoral genetics & KVB veterinary field dispatch</p>
        </div>

        @if (errorMessage()) {
          <div class="alert alert-danger">{{ errorMessage() }}</div>
        }

        <form [formGroup]="registerForm" (ngSubmit)="submitRegister()">
          <!-- Role Selector -->
          <div class="form-group">
            <label class="group-label">Select Your Operating Role</label>
            <div class="role-grid">
              <label class="role-card" [class.selected]="registerForm.get('role')?.value === 'USER'">
                <input type="radio" formControlName="role" value="USER" class="sr-only" />
                <span class="role-icon">🧑‍🌾</span>
                <span class="role-title">Farmer / Breeder</span>
                <span class="role-desc">Manage herd estrus alerts & source certified AI semen straws</span>
              </label>

              <label class="role-card" [class.selected]="registerForm.get('role')?.value === 'VET'">
                <input type="radio" formControlName="role" value="VET" class="sr-only" />
                <span class="role-icon">👨‍⚕️</span>
                <span class="role-title">KVB Veterinary Officer</span>
                <span class="role-desc">Receive emergency rural heat calls & field clinical jobs</span>
              </label>
            </div>
          </div>

          <!-- KVB Regulatory Trust Badge -->
          <div class="kvb-trust-badge" [class.highlight]="registerForm.get('role')?.value === 'VET'">
            <span class="trust-icon">🛡️</span>
            <div class="trust-content">
              <strong>Kenya Veterinary Board (KVB) Verified Network</strong>
              <span>
                {{ registerForm.get('role')?.value === 'VET' 
                  ? 'Active KVB registration and certificate number will be verified during onboarding.' 
                  : 'All artificial inseminations performed by licensed KVB veterinary officers.' }}
              </span>
            </div>
          </div>

          <!-- Form Fields -->
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

          <!-- Dynamic Role-Based Action Button -->
          <button
            type="submit"
            class="btn-arva-submit"
            [disabled]="isLoading() || registerForm.invalid">
            @if (isLoading()) {
              <span>Creating Account...</span>
            } @else if (registerForm.get('role')?.value === 'VET') {
              <span>Register as KVB Veterinary Officer →</span>
            } @else {
              <span>Join as Farmer / Breeder →</span>
            }
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
      min-height: 85vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      background: var(--color-cream);
    }

    .register-card {
      background: #ffffff;
      border: 1px solid rgba(7, 80, 63, 0.12);
      border-radius: 30px;
      padding: 48px 40px;
      max-width: 520px;
      width: 100%;
      box-shadow: 0 16px 40px rgba(7, 80, 63, 0.05);

      @media (max-width: 540px) {
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

      .group-label, label {
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

    .role-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 12px;
      margin-bottom: 8px;

      @media (max-width: 480px) {
        grid-template-columns: 1fr;
      }
    }

    .role-card {
      border: 1.5px solid rgba(0, 0, 0, 0.1);
      background: var(--color-mist);
      border-radius: 15px;
      padding: 16px 12px;
      cursor: pointer;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      transition: all 0.25s ease;

      &.selected {
        border-color: var(--color-forest);
        background: #f4f8f6;
        box-shadow: 0 4px 12px rgba(7, 80, 63, 0.08);

        .role-title {
          color: var(--color-forest);
        }
      }

      .role-icon {
        font-size: 26px;
        margin-bottom: 6px;
      }

      .role-title {
        font-family: var(--font-sans);
        font-weight: 600;
        font-size: 13.5px;
        color: var(--color-deep-ink);
      }

      .role-desc {
        font-family: var(--font-sans);
        font-size: 11.5px;
        line-height: 1.35;
        color: var(--color-muted);
        margin-top: 4px;
      }
    }

    .kvb-trust-badge {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      background: rgba(195, 205, 167, 0.18);
      border: 1px solid var(--color-sage);
      border-radius: 12px;
      padding: 10px 14px;
      margin-bottom: 20px;
      transition: all 0.3s ease;

      &.highlight {
        background: rgba(232, 254, 133, 0.2);
        border-color: var(--color-forest);
      }

      .trust-icon {
        font-size: 18px;
        line-height: 1;
        flex-shrink: 0;
      }

      .trust-content {
        font-size: 12px;
        line-height: 1.4;
        color: var(--color-forest);

        strong {
          display: block;
          font-weight: 600;
          color: var(--color-forest);
          margin-bottom: 2px;
        }
        span {
          color: #4a5d4e;
        }
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
