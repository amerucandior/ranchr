import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ListingService } from '../../services/listing.service';
import { Gender, ListingServiceType } from '../../models/listing.models';

@Component({
  selector: 'app-create-listing',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="create-listing-wrapper">
      <div class="header-nav">
        <a routerLink="/marketplace" class="back-link">← Back to Marketplace</a>
      </div>

      <div class="card">
        <div class="card-title">
          <span class="icon">🐂</span>
          <div>
            <h2>Publish Livestock Listing</h2>
            <p class="subtitle">Showcase dairy & beef breeding stock, artificial insemination straws, and milk metrics.</p>
          </div>
        </div>

        @if (errorMessage()) {
          <div class="alert alert-danger">{{ errorMessage() }}</div>
        }

        <form [formGroup]="listingForm" (ngSubmit)="submitListing()">
          <div class="form-grid">
            <div class="form-group">
              <label for="animalType">Animal Type *</label>
              <input
                id="animalType"
                type="text"
                class="form-control"
                formControlName="animalType"
                placeholder="e.g. Dairy Cow, Stud Bull, Heifer" />
            </div>

            <div class="form-group">
              <label for="breed">Breed *</label>
              <input
                id="breed"
                type="text"
                class="form-control"
                formControlName="breed"
                placeholder="e.g. Friesian, Ayrshire, Jersey, Boran" />
            </div>

            <div class="form-group">
              <label for="gender">Gender</label>
              <select id="gender" class="form-control" formControlName="gender">
                <option value="FEMALE">Female (Cow / Heifer)</option>
                <option value="MALE">Male (Bull / Sire)</option>
              </select>
            </div>

            <div class="form-group">
              <label for="location">Location (County / Town) *</label>
              <input
                id="location"
                type="text"
                class="form-control"
                formControlName="location"
                placeholder="e.g. Nakuru, Njoro, Eldoret" />
            </div>

            <div class="form-group">
              <label for="serviceType">Service / Listing Type *</label>
              <select id="serviceType" class="form-control" formControlName="serviceType">
                <option value="ARTIFICIAL_INSEMINATION">Artificial Insemination (AI Straw)</option>
                <option value="NATURAL">Natural Service</option>
                <option value="BOTH">Both Natural & AI</option>
              </select>
            </div>

            <div class="form-group">
              <label for="fee">Listing / Service Fee (KES) *</label>
              <input
                id="fee"
                type="number"
                min="0"
                class="form-control"
                formControlName="fee"
                placeholder="e.g. 75000" />
            </div>

            <div class="form-group">
              <label for="ageYears">Age (Years)</label>
              <input
                id="ageYears"
                type="number"
                min="0"
                max="30"
                class="form-control"
                formControlName="ageYears"
                placeholder="e.g. 3" />
            </div>

            <div class="form-group">
              <label for="ageMonths">Age (Months)</label>
              <input
                id="ageMonths"
                type="number"
                min="0"
                max="11"
                class="form-control"
                formControlName="ageMonths"
                placeholder="e.g. 6" />
            </div>
          </div>

          <div class="form-group">
            <label for="description">Genetics & Production Details</label>
            <textarea
              id="description"
              rows="3"
              class="form-control"
              formControlName="description"
              placeholder="e.g. Daily yield 28-32 Litres, pedigree sire KAGRC, disease free with updated vaccination records..."></textarea>
          </div>

          <!-- Cloudinary Multi-image Upload -->
          <div class="form-group upload-section">
            <label>Upload Animal Photos (Max 10 Images)</label>
            <input
              type="file"
              multiple
              accept="image/*"
              class="file-input"
              (change)="onFilesSelected($event)" />

            @if (selectedFiles.length > 0) {
              <div class="preview-grid">
                @for (file of selectedFiles; track file.name; let i = $index) {
                  <div class="preview-item">
                    <span class="file-name">📷 {{ file.name }}</span>
                    <button type="button" class="btn-remove-file" (click)="removeFile(i)">✕</button>
                  </div>
                }
              </div>
            }
          </div>

          <button
            type="submit"
            class="btn-submit"
            [disabled]="isLoading() || listingForm.invalid">
            {{ isLoading() ? 'Uploading Media & Publishing...' : 'Publish Livestock Listing' }}
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .create-listing-wrapper {
      max-width: 800px;
      margin: 2rem auto;
      padding: 0 1rem;
    }
    .header-nav {
      margin-bottom: 1rem;
      .back-link {
        color: #15803d;
        text-decoration: none;
        font-weight: 600;
        font-size: 0.9rem;
        &:hover {
          text-decoration: underline;
        }
      }
    }
    .card {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 2rem;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
    }
    .card-title {
      display: flex;
      gap: 1rem;
      align-items: flex-start;
      margin-bottom: 1.5rem;
      .icon {
        font-size: 2rem;
      }
      h2 {
        font-size: 1.4rem;
        color: #0f172a;
        margin: 0;
      }
      .subtitle {
        color: #64748b;
        font-size: 0.85rem;
        margin: 0.25rem 0 0 0;
      }
    }
    .alert-danger {
      background: #fef2f2;
      border: 1px solid #fca5a5;
      color: #991b1b;
      padding: 0.75rem 1rem;
      border-radius: 8px;
      font-size: 0.85rem;
      margin-bottom: 1.25rem;
    }
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }
    @media (max-width: 640px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
    }
    .form-group {
      margin-bottom: 1.25rem;
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
        font-size: 0.9rem;
      }
    }
    .upload-section {
      background: #f8fafc;
      border: 1px dashed #cbd5e1;
      padding: 1.25rem;
      border-radius: 8px;
      .file-input {
        width: 100%;
      }
    }
    .preview-grid {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      margin-top: 0.75rem;
      .preview-item {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        background: #ffffff;
        border: 1px solid #e2e8f0;
        padding: 0.35rem 0.6rem;
        border-radius: 6px;
        font-size: 0.8rem;
        .file-name {
          max-width: 200px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .btn-remove-file {
          background: none;
          border: none;
          color: #ef4444;
          cursor: pointer;
          font-weight: 700;
        }
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
      font-size: 1rem;
      cursor: pointer;
      margin-top: 0.5rem;
      &:hover:not(:disabled) {
        background: #166534;
      }
      &:disabled {
        opacity: 0.6;
      }
    }
  `],
})
export class CreateListingComponent {
  private readonly fb = inject(FormBuilder);
  private readonly listingService = inject(ListingService);
  private readonly router = inject(Router);

  readonly isLoading = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);

  selectedFiles: File[] = [];

  listingForm = this.fb.group({
    animalType: ['', Validators.required],
    breed: ['', Validators.required],
    gender: ['FEMALE' as Gender, Validators.required],
    location: ['', Validators.required],
    serviceType: ['ARTIFICIAL_INSEMINATION' as ListingServiceType, Validators.required],
    fee: [50000, [Validators.required, Validators.min(0)]],
    ageYears: [2],
    ageMonths: [6],
    description: [''],
  });

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      const files = Array.from(input.files);
      if (this.selectedFiles.length + files.length > 10) {
        this.errorMessage.set('Maximum 10 images allowed per listing.');
        return;
      }
      this.selectedFiles.push(...files);
    }
  }

  removeFile(index: number): void {
    this.selectedFiles.splice(index, 1);
  }

  submitListing(): void {
    if (this.listingForm.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const formVal = this.listingForm.value;

    this.listingService
      .create(
        {
          animalType: formVal.animalType!,
          breed: formVal.breed!,
          gender: formVal.gender as Gender,
          location: formVal.location!,
          serviceType: formVal.serviceType as ListingServiceType,
          fee: formVal.fee ?? 0,
          ageYears: formVal.ageYears ?? 0,
          ageMonths: formVal.ageMonths ?? 0,
          description: formVal.description ?? '',
        },
        this.selectedFiles
      )
      .subscribe({
        next: () => {
          this.isLoading.set(false);
          this.router.navigate(['/marketplace']);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error?.message || 'Failed to create listing. Please try again.');
        },
      });
  }
}
