import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { KycService } from '../../../../core/services/kyc.service';
import { KycStatus, ServicesOffered, VetProfileResponse } from '../../../../core/models/kyc.models';

@Component({
  selector: 'app-kyc-verification',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="kyc-container">
      <div class="kyc-header">
        <h2>Identity & Accreditation</h2>
        <p class="subtitle">Kenya Veterinary Board (KVB) accreditation and verified SMS dispatch credentials.</p>
      </div>

      @if (statusMessage()) {
        <div class="alert alert-success">{{ statusMessage() }}</div>
      }
      @if (errorMessage()) {
        <div class="alert alert-danger">{{ errorMessage() }}</div>
      }

      <!-- Section 1: Phone & National ID Verification -->
      <div class="card">
        <div class="card-title">
          <span class="icon">📱</span>
          <div>
            <h3>Phone & National ID Verification</h3>
            <p>Required for two-way SMS alerts, dispatch notifications, and marketplace interactions.</p>
          </div>
        </div>

        @if (kycStatus()?.verified) {
          <div class="verified-badge">
            <span class="badge-icon">✓</span>
            <div class="badge-content">
              <strong>Verified Identity</strong>
              <div class="details">
                <span>Phone: {{ kycStatus()?.phoneNumber }}</span>
                <span>National ID: {{ kycStatus()?.nationalId }}</span>
                @if (kycStatus()?.verifiedAt) {
                  <span>Verified: {{ kycStatus()?.verifiedAt | date:'medium' }}</span>
                }
              </div>
            </div>
          </div>
        } @else {
          @if (otpStep() === 1) {
            <form [formGroup]="phoneForm" (ngSubmit)="sendOtp()">
              <div class="form-grid">
                <div class="form-group">
                  <label for="phoneNumber">Kenyan Phone Number</label>
                  <input
                    id="phoneNumber"
                    type="text"
                    class="form-control"
                    formControlName="phoneNumber"
                    placeholder="+254712345678" />
                  <small class="helper-text">Format: +2547xxxxxxxx or +2541xxxxxxxx</small>
                </div>

                <div class="form-group">
                  <label for="nationalId">National ID Number</label>
                  <input
                    id="nationalId"
                    type="text"
                    class="form-control"
                    formControlName="nationalId"
                    placeholder="7 to 8 digits" />
                </div>
              </div>

              <button
                type="submit"
                class="btn-primary"
                [disabled]="isLoading() || phoneForm.invalid">
                {{ isLoading() ? 'Sending SMS OTP...' : 'Send Verification OTP' }}
              </button>
            </form>
          } @else {
            <form [formGroup]="otpForm" (ngSubmit)="confirmOtp()">
              <div class="otp-box">
                <p>An SMS with a 6-digit verification code was sent to <strong>{{ phoneForm.get('phoneNumber')?.value }}</strong>.</p>
                <div class="form-group">
                  <label for="otpCode">Enter 6-Digit OTP Code</label>
                  <input
                    id="otpCode"
                    type="text"
                    class="form-control otp-input"
                    formControlName="code"
                    maxlength="6"
                    placeholder="123456" />
                </div>

                <div class="btn-row">
                  <button
                    type="submit"
                    class="btn-primary"
                    [disabled]="isLoading() || otpForm.invalid">
                    {{ isLoading() ? 'Verifying...' : 'Verify Phone Number' }}
                  </button>
                  <button
                    type="button"
                    class="btn-secondary"
                    (click)="otpStep.set(1)">
                    Change Phone Number
                  </button>
                </div>
              </div>
            </form>
          }
        }
      </div>

      <!-- Section 2: Veterinary Officer KVB Enlistment & Credentials -->
      @if (authService.isVet()) {
        <div class="card mt-4">
          <div class="card-title">
            <span class="icon">🩺</span>
            <div>
              <h3>Kenya Veterinary Board (KVB) Accreditation</h3>
              <p>KVB registration license and practicing credential validation for clinical dispatch.</p>
            </div>
          </div>

          @if (vetProfile()) {
            <div class="status-banner" [ngClass]="vetProfile()?.status">
              <span class="status-indicator"></span>
              <div>
                <strong>Accreditation Status: {{ vetProfile()?.status }}</strong>
                <p class="status-desc">
                  @if (vetProfile()?.status === 'APPROVED') {
                    Your credentials have been verified by the Kenya Veterinary Board. You can accept dispatch alerts.
                  } @else if (vetProfile()?.status === 'REJECTED') {
                    Your documents could not be approved. Please update your license documents below.
                  } @else {
                    Your credentials are under review by the registry administrator.
                  }
                </p>
              </div>
            </div>
          }

          <form [formGroup]="vetForm" (ngSubmit)="submitVetProfile()">
            <div class="form-group">
              <label for="experienceYears">Years of Clinical Experience</label>
              <input
                id="experienceYears"
                type="number"
                min="0"
                max="60"
                class="form-control"
                formControlName="experienceYears"
                placeholder="e.g. 5" />
            </div>

            <div class="form-group">
              <label>Services Offered</label>
              <div class="services-grid">
                @for (service of availableServices; track service.id) {
                  <label class="checkbox-label">
                    <input
                      type="checkbox"
                      [checked]="isServiceSelected(service.id)"
                      (change)="toggleService(service.id)" />
                    <span>{{ service.label }}</span>
                  </label>
                }
              </div>
            </div>

            <div class="form-group">
              <label for="kvbRegisteredName">Registered Full Name with KVB</label>
              <input
                id="kvbRegisteredName"
                type="text"
                class="form-control"
                formControlName="kvbRegisteredName"
                placeholder="Dr. Full Name" />
            </div>

            <div class="upload-grid">
              <div class="upload-item">
                <label>KVB License Document (Photo / PDF)</label>
                <input
                  type="file"
                  class="file-input"
                  (change)="onFileChange($event, 'license')" />
              </div>

              <div class="upload-item">
                <label>National ID Front</label>
                <input
                  type="file"
                  class="file-input"
                  (change)="onFileChange($event, 'idFront')" />
              </div>

              <div class="upload-item">
                <label>National ID Back</label>
                <input
                  type="file"
                  class="file-input"
                  (change)="onFileChange($event, 'idBack')" />
              </div>
            </div>

            <button
              type="submit"
              class="btn-primary mt-3"
              [disabled]="isLoading() || selectedServices().length === 0">
              {{ isLoading() ? 'Saving Documents...' : 'Save & Submit KVB Credentials' }}
            </button>
          </form>
        </div>
      }
    </div>
  `,
  styles: [`
    .kyc-container {
      max-width: 800px;
      margin: 2rem auto;
      padding: 0 1rem;
    }
    .kyc-header {
      margin-bottom: 1.5rem;
      h2 {
        font-size: 1.75rem;
        color: #0f172a;
        margin-bottom: 0.25rem;
      }
      .subtitle {
        color: #64748b;
        font-size: 0.9rem;
      }
    }
    .card {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      margin-bottom: 1.5rem;
    }
    .mt-4 {
      margin-top: 1.5rem;
    }
    .mt-3 {
      margin-top: 1rem;
    }
    .card-title {
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
      margin-bottom: 1.25rem;
      .icon {
        font-size: 1.75rem;
      }
      h3 {
        font-size: 1.15rem;
        color: #0f172a;
        margin: 0;
      }
      p {
        font-size: 0.8rem;
        color: #64748b;
        margin: 0.2rem 0 0 0;
      }
    }
    .alert {
      padding: 0.75rem 1rem;
      border-radius: 8px;
      font-size: 0.85rem;
      margin-bottom: 1rem;
      &.alert-success {
        background: #f0fdf4;
        border: 1px solid #86efac;
        color: #166534;
      }
      &.alert-danger {
        background: #fef2f2;
        border: 1px solid #fca5a5;
        color: #991b1b;
      }
    }
    .verified-badge {
      display: flex;
      align-items: center;
      gap: 1rem;
      background: #f0fdf4;
      border: 1px solid #86efac;
      padding: 1rem;
      border-radius: 8px;
      .badge-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        background: #15803d;
        color: #ffffff;
        font-size: 1.25rem;
        font-weight: 800;
        border-radius: 50%;
      }
      .badge-content {
        strong {
          color: #15803d;
          font-size: 0.95rem;
        }
        .details {
          display: flex;
          flex-wrap: wrap;
          gap: 1rem;
          font-size: 0.8rem;
          color: #334155;
          margin-top: 0.25rem;
        }
      }
    }
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
      margin-bottom: 1rem;
    }
    @media (max-width: 640px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
    }
    .form-group {
      margin-bottom: 1rem;
      label {
        display: block;
        font-weight: 600;
        font-size: 0.85rem;
        color: #334155;
        margin-bottom: 0.35rem;
      }
      .form-control {
        width: 100%;
        padding: 0.75rem;
        border: 1px solid #cbd5e1;
        border-radius: 8px;
        box-sizing: border-box;
      }
      .helper-text {
        display: block;
        font-size: 0.75rem;
        color: #64748b;
        margin-top: 0.25rem;
      }
    }
    .otp-input {
      font-size: 1.5rem;
      letter-spacing: 0.5rem;
      text-align: center;
      max-width: 250px;
    }
    .btn-row {
      display: flex;
      gap: 0.75rem;
      margin-top: 1rem;
    }
    .btn-primary {
      padding: 0.75rem 1.5rem;
      background: #15803d;
      color: #ffffff;
      border: none;
      border-radius: 8px;
      font-weight: 700;
      cursor: pointer;
      &:hover:not(:disabled) {
        background: #166534;
      }
      &:disabled {
        opacity: 0.6;
      }
    }
    .btn-secondary {
      padding: 0.75rem 1rem;
      background: #f1f5f9;
      color: #334155;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      &:hover {
        background: #e2e8f0;
      }
    }
    .status-banner {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.85rem 1rem;
      border-radius: 8px;
      margin-bottom: 1.25rem;
      &.APPROVED {
        background: #f0fdf4;
        border: 1px solid #86efac;
        color: #166534;
      }
      &.UNDER_REVIEW {
        background: #fffbeb;
        border: 1px solid #fde68a;
        color: #92400e;
      }
      &.REJECTED {
        background: #fef2f2;
        border: 1px solid #fca5a5;
        color: #991b1b;
      }
      .status-desc {
        font-size: 0.8rem;
        margin: 0.2rem 0 0 0;
      }
    }
    .services-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 0.5rem;
      .checkbox-label {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 0.85rem;
        color: #334155;
        cursor: pointer;
      }
    }
    .upload-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 1rem;
      margin-top: 1rem;
      .upload-item {
        label {
          display: block;
          font-size: 0.8rem;
          font-weight: 600;
          color: #475569;
          margin-bottom: 0.35rem;
        }
        .file-input {
          font-size: 0.8rem;
          width: 100%;
        }
      }
    }
  `],
})
export class KycVerificationComponent implements OnInit {
  readonly authService = inject(AuthService);
  private readonly kycService = inject(KycService);
  private readonly fb = inject(FormBuilder);

  readonly isLoading = signal<boolean>(false);
  readonly statusMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly kycStatus = signal<KycStatus | null>(null);
  readonly vetProfile = signal<VetProfileResponse | null>(null);
  readonly otpStep = signal<number>(1);
  readonly selectedServices = signal<ServicesOffered[]>([
    'ARTIFICIAL_INSEMINATION',
    'PREGNANCY_DIAGNOSIS',
  ]);

  licenseFile: File | null = null;
  idFrontFile: File | null = null;
  idBackFile: File | null = null;

  readonly availableServices: { id: ServicesOffered; label: string }[] = [
    { id: 'ARTIFICIAL_INSEMINATION', label: 'Artificial Insemination (AI)' },
    { id: 'PREGNANCY_DIAGNOSIS', label: 'Pregnancy Diagnosis' },
    { id: 'EMERGENCY_OBSTETRICS', label: 'Emergency Obstetrics' },
    { id: 'ROUTINE_CHECKUP', label: 'Routine Herd Checkup' },
    { id: 'VACCINATION', label: 'Vaccination' },
    { id: 'SURGERY', label: 'Clinical Surgery' },
    { id: 'HERD_HEALTH_CONSULTING', label: 'Herd Health Consulting' },
  ];

  phoneForm = this.fb.group({
    phoneNumber: ['', [Validators.required, Validators.pattern('^\\+254(?:7\\d{8}|1\\d{8})$')]],
    nationalId: ['', [Validators.required, Validators.minLength(7), Validators.maxLength(8)]],
  });

  otpForm = this.fb.group({
    code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
  });

  vetForm = this.fb.group({
    experienceYears: [3, [Validators.min(0), Validators.max(60)]],
    kvbRegisteredName: ['', Validators.required],
  });

  ngOnInit(): void {
    this.loadKycStatus();
  }

  loadKycStatus(): void {
    this.kycService.getKycStatus().subscribe({
      next: (status) => this.kycStatus.set(status),
      error: () => {},
    });
  }

  sendOtp(): void {
    if (this.phoneForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.statusMessage.set(null);

    const { phoneNumber, nationalId } = this.phoneForm.value;

    this.kycService
      .requestOtp({
        phoneNumber: phoneNumber!,
        nationalId: nationalId!,
      })
      .subscribe({
        next: (msg) => {
          this.isLoading.set(false);
          this.statusMessage.set(msg || 'OTP sent via SMS.');
          this.otpStep.set(2);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error?.message || err.error || 'Failed to send OTP. Check phone number format.');
        },
      });
  }

  confirmOtp(): void {
    if (this.otpForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.statusMessage.set(null);

    const { code } = this.otpForm.value;

    this.kycService.verifyOtp({ code: code! }).subscribe({
      next: (msg) => {
        this.isLoading.set(false);
        this.statusMessage.set(msg || 'Phone number and national ID verified successfully.');
        this.loadKycStatus();
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || err.error || 'Invalid or expired OTP code.');
      },
    });
  }

  isServiceSelected(service: ServicesOffered): boolean {
    return this.selectedServices().includes(service);
  }

  toggleService(service: ServicesOffered): void {
    const current = this.selectedServices();
    if (current.includes(service)) {
      this.selectedServices.set(current.filter((s) => s !== service));
    } else {
      this.selectedServices.set([...current, service]);
    }
  }

  onFileChange(event: Event, type: 'license' | 'idFront' | 'idBack'): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      if (type === 'license') this.licenseFile = file;
      if (type === 'idFront') this.idFrontFile = file;
      if (type === 'idBack') this.idBackFile = file;
    }
  }

  submitVetProfile(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.statusMessage.set(null);

    const { experienceYears, kvbRegisteredName } = this.vetForm.value;

    // 1. First ensure enlistment
    this.kycService
      .enlistVetProfile({
        servicesOffered: this.selectedServices(),
        experienceYears: experienceYears ?? 0,
      })
      .subscribe({
        next: () => {
          // 2. Upload verification docs if present
          const formData = new FormData();
          if (kvbRegisteredName) formData.append('kvbRegisteredName', kvbRegisteredName);
          if (this.licenseFile) formData.append('kvbLicenseNumber', this.licenseFile);
          if (this.idFrontFile) formData.append('idCardFront', this.idFrontFile);
          if (this.idBackFile) formData.append('idCardBack', this.idBackFile);

          this.kycService.updateKvbVerification(formData).subscribe({
            next: (profile) => {
              this.isLoading.set(false);
              this.vetProfile.set(profile);
              this.statusMessage.set('KVB credentials submitted successfully for review.');
            },
            error: (err) => {
              this.isLoading.set(false);
              this.errorMessage.set(err.error?.message || 'Failed to upload KVB documents.');
            },
          });
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error?.message || 'Failed to enlist veterinary profile.');
        },
      });
  }
}
