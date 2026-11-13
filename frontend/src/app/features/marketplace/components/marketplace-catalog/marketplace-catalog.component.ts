import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ListingService } from '../../services/listing.service';
import { ListingResponse, ListingServiceType } from '../../models/listing.models';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-marketplace-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="marketplace-container">
      <!-- Header Banner -->
      <div class="marketplace-header">
        <div>
          <h2>Livestock Marketplace & Genetics</h2>
          <p class="subtitle">Verified dairy and beef breeding stock, artificial insemination services, and farm listings.</p>
        </div>
        <div class="header-actions">
          <a routerLink="/marketplace/new" class="btn-list-animal">
            + List Your Livestock
          </a>
        </div>
      </div>

      <!-- Filters & Search Toolbar -->
      <div class="search-toolbar">
        <div class="search-inputs">
          <div class="search-field">
            <span class="field-icon">🔍</span>
            <input
              type="text"
              [(ngModel)]="searchKeyword"
              (keyup.enter)="loadListings()"
              placeholder="Filter by animal type, breed (e.g. Friesian, Ayrshire)..." />
          </div>

          <div class="search-field">
            <span class="field-icon">📍</span>
            <input
              type="text"
              [(ngModel)]="searchLocation"
              (keyup.enter)="loadListings()"
              placeholder="Location (e.g. Nakuru, Eldoret)..." />
          </div>
        </div>

        <div class="filter-chips">
          <button
            type="button"
            class="chip"
            [class.active]="selectedServiceType === null"
            (click)="selectServiceType(null)">
            All Types
          </button>
          <button
            type="button"
            class="chip"
            [class.active]="selectedServiceType === 'ARTIFICIAL_INSEMINATION'"
            (click)="selectServiceType('ARTIFICIAL_INSEMINATION')">
            Artificial Insemination
          </button>
          <button
            type="button"
            class="chip"
            [class.active]="selectedServiceType === 'NATURAL'"
            (click)="selectServiceType('NATURAL')">
            Natural Service
          </button>
          <button
            type="button"
            class="chip"
            [class.active]="selectedServiceType === 'BOTH'"
            (click)="selectServiceType('BOTH')">
            Dual Service
          </button>
        </div>
      </div>

      <!-- Listings Grid -->
      @if (isLoading()) {
        <div class="loading-state">
          <div class="spinner"></div>
          <p>Scanning livestock catalog across Kenya...</p>
        </div>
      } @else if (listings().length === 0) {
        <div class="empty-state">
          <span class="empty-icon">🐄</span>
          <h3>No livestock listings found</h3>
          <p>No listings matched your criteria. Try adjusting your search keyword or location.</p>
          <button (click)="resetFilters()" class="btn-reset">Reset Filters</button>
        </div>
      } @else {
        <div class="listings-grid">
          @for (item of listings(); track item.id) {
            <div class="listing-card">
              <!-- Media banner -->
              <div class="card-media">
                @if (item.images && item.images.length > 0) {
                  <img [src]="item.images[0].url" [alt]="item.breed || item.animalType" class="listing-img" />
                } @else {
                  <div class="placeholder-img">
                    <span>{{ item.gender === 'MALE' ? '🐂' : '🐄' }}</span>
                  </div>
                }
                <span class="service-pill" [ngClass]="item.serviceType">
                  {{ item.serviceType === 'ARTIFICIAL_INSEMINATION' ? 'AI Straw' : item.serviceType }}
                </span>
              </div>

              <!-- Content -->
              <div class="card-body">
                <div class="title-row">
                  <h4 class="animal-title">{{ item.breed || item.animalType }}</h4>
                  <span class="gender-pill">{{ item.gender || 'LIVESTOCK' }}</span>
                </div>

                <div class="meta-row">
                  <span class="meta-tag">📍 {{ item.location }}</span>
                  @if (item.ageYears !== undefined && item.ageYears !== null) {
                    <span class="meta-tag">⏳ {{ item.ageYears }}y {{ item.ageMonths || 0 }}m</span>
                  }
                </div>

                <p class="desc">{{ item.description || 'Verified livestock listing available for inspection and breeding service.' }}</p>

                <div class="card-footer">
                  <div class="price-box">
                    <span class="price-label">Fee / Price</span>
                    <span class="price-val">KES {{ item.fee | number }}</span>
                  </div>

                  <div class="action-buttons">
                    <button
                      type="button"
                      class="btn-interest"
                      title="Show interest"
                      (click)="expressInterest(item)">
                      ❤️ {{ item.interestCount }}
                    </button>
                    <a [routerLink]="['/connections']" [queryParams]="{ listingId: item.id }" class="btn-connect">
                      Inquire
                    </a>
                  </div>
                </div>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .marketplace-container {
      max-width: 1200px;
      margin: 1.5rem auto;
      padding: 0 1rem;
    }
    .marketplace-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
      flex-wrap: wrap;
      gap: 1rem;
      h2 {
        font-size: 1.75rem;
        color: #0f172a;
        margin: 0;
      }
      .subtitle {
        color: #64748b;
        font-size: 0.9rem;
        margin: 0.25rem 0 0 0;
      }
    }
    .btn-list-animal {
      display: inline-flex;
      align-items: center;
      padding: 0.75rem 1.25rem;
      background: #15803d;
      color: #ffffff;
      border-radius: 8px;
      font-weight: 700;
      text-decoration: none;
      font-size: 0.9rem;
      transition: background 0.2s;
      &:hover {
        background: #166534;
      }
    }
    .search-toolbar {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      padding: 1.25rem;
      margin-bottom: 1.5rem;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
    }
    .search-inputs {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 0.75rem;
      margin-bottom: 1rem;
    }
    @media (max-width: 640px) {
      .search-inputs {
        grid-template-columns: 1fr;
      }
    }
    .search-field {
      display: flex;
      align-items: center;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      padding: 0.5rem 0.75rem;
      background: #f8fafc;
      .field-icon {
        margin-right: 0.5rem;
      }
      input {
        border: none;
        background: transparent;
        outline: none;
        width: 100%;
        font-size: 0.9rem;
      }
    }
    .filter-chips {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
      .chip {
        padding: 0.4rem 0.85rem;
        border-radius: 20px;
        border: 1px solid #cbd5e1;
        background: #f1f5f9;
        font-size: 0.8rem;
        font-weight: 600;
        color: #475569;
        cursor: pointer;
        transition: all 0.2s;
        &.active {
          background: #15803d;
          border-color: #15803d;
          color: #ffffff;
        }
      }
    }
    .loading-state, .empty-state {
      text-align: center;
      padding: 4rem 1rem;
      background: #ffffff;
      border: 1px dashed #cbd5e1;
      border-radius: 12px;
      .empty-icon {
        font-size: 3rem;
      }
      h3 {
        color: #0f172a;
        margin: 0.5rem 0 0.25rem;
      }
      p {
        color: #64748b;
        font-size: 0.9rem;
      }
      .btn-reset {
        margin-top: 1rem;
        padding: 0.5rem 1rem;
        background: #e2e8f0;
        border: none;
        border-radius: 6px;
        cursor: pointer;
      }
    }
    .spinner {
      width: 40px;
      height: 40px;
      border: 3px solid #e2e8f0;
      border-top-color: #15803d;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin: 0 auto 1rem;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    .listings-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
      gap: 1.5rem;
    }
    .listing-card {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 12px;
      overflow: hidden;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      display: flex;
      flex-direction: column;
      transition: transform 0.2s, box-shadow 0.2s;
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
      }
    }
    .card-media {
      position: relative;
      height: 180px;
      background: #f1f5f9;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      .listing-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      .placeholder-img {
        font-size: 4rem;
      }
      .service-pill {
        position: absolute;
        top: 0.75rem;
        right: 0.75rem;
        padding: 0.25rem 0.6rem;
        border-radius: 4px;
        font-size: 0.7rem;
        font-weight: 800;
        letter-spacing: 0.05em;
        text-transform: uppercase;
        background: rgba(15, 23, 42, 0.85);
        color: #ffffff;
        backdrop-filter: blur(4px);
        &.ARTIFICIAL_INSEMINATION {
          background: #0284c7;
        }
        &.NATURAL {
          background: #d97706;
        }
      }
    }
    .card-body {
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      flex-grow: 1;
      .title-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 0.4rem;
        .animal-title {
          font-size: 1.1rem;
          color: #0f172a;
          margin: 0;
        }
        .gender-pill {
          font-size: 0.65rem;
          font-weight: 700;
          padding: 0.15rem 0.4rem;
          border-radius: 4px;
          background: #f1f5f9;
          color: #475569;
        }
      }
      .meta-row {
        display: flex;
        gap: 0.5rem;
        margin-bottom: 0.75rem;
        .meta-tag {
          font-size: 0.75rem;
          color: #64748b;
        }
      }
      .desc {
        font-size: 0.85rem;
        color: #475569;
        margin: 0 0 1rem 0;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      .card-footer {
        margin-top: auto;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 0.75rem;
        border-top: 1px solid #f1f5f9;
        .price-box {
          display: flex;
          flex-direction: column;
          .price-label {
            font-size: 0.65rem;
            color: #94a3b8;
            text-transform: uppercase;
            font-weight: 700;
          }
          .price-val {
            font-size: 1.15rem;
            font-weight: 800;
            color: #15803d;
          }
        }
        .action-buttons {
          display: flex;
          gap: 0.5rem;
          align-items: center;
          .btn-interest {
            padding: 0.4rem 0.6rem;
            border-radius: 6px;
            border: 1px solid #e2e8f0;
            background: #ffffff;
            cursor: pointer;
            font-size: 0.8rem;
            &:hover {
              background: #fef2f2;
              border-color: #fca5a5;
            }
          }
          .btn-connect {
            padding: 0.45rem 0.85rem;
            background: #0f172a;
            color: #ffffff;
            border-radius: 6px;
            font-size: 0.8rem;
            font-weight: 700;
            text-decoration: none;
            &:hover {
              background: #1e293b;
            }
          }
        }
      }
    }
  `],
})
export class MarketplaceCatalogComponent implements OnInit {
  private readonly listingService = inject(ListingService);
  readonly authService = inject(AuthService);

  readonly listings = signal<ListingResponse[]>([]);
  readonly isLoading = signal<boolean>(false);

  searchKeyword = '';
  searchLocation = '';
  selectedServiceType: ListingServiceType | null = null;

  ngOnInit(): void {
    this.loadListings();
  }

  loadListings(): void {
    this.isLoading.set(true);

    this.listingService
      .search({
        animalType: this.searchKeyword.trim() || undefined,
        location: this.searchLocation.trim() || undefined,
        serviceType: this.selectedServiceType || undefined,
      })
      .subscribe({
        next: (page) => {
          this.listings.set(page.content || []);
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
        },
      });
  }

  selectServiceType(type: ListingServiceType | null): void {
    this.selectedServiceType = type;
    this.loadListings();
  }

  resetFilters(): void {
    this.searchKeyword = '';
    this.searchLocation = '';
    this.selectedServiceType = null;
    this.loadListings();
  }

  expressInterest(item: ListingResponse): void {
    this.listingService.recordInterest(item.id).subscribe({
      next: () => {
        item.interestCount++;
      },
      error: () => {},
    });
  }
}
