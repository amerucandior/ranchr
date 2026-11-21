import { IconComponent } from '../../../../shared/components/icon/icon.component';
import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ConnectionService, ConnectionRequestResponse } from '../../../../core/services/connection.service';
import { ListingService } from '../../services/listing.service';
import { ListingResponse } from '../../models/listing.models';

@Component({
  selector: 'app-connections',
  standalone: true,
  imports: [IconComponent, CommonModule, FormsModule, RouterLink],
  template: `
    <div class="connections-wrapper">
      <div class="connections-header">
        <h2>Livestock Connections & Inquiries</h2>
        <p class="subtitle">Direct introductions between livestock buyers and animal owners.</p>
      </div>

      @if (statusMessage()) {
        <div class="alert alert-success">{{ statusMessage() }}</div>
      }
      @if (errorMessage()) {
        <div class="alert alert-danger">{{ errorMessage() }}</div>
      }

      <!-- Send Connection Box (When query param listingId is present) -->
      @if (targetListingId()) {
        <div class="inquiry-card">
          <div class="inquiry-header">
            <span class="icon"><app-icon name="mail" /></span>
            <div>
              <h3>Send Inquiry to Seller</h3>
              <p>Express interest in this animal listing. Once accepted, seller contact details will be shared.</p>
            </div>
          </div>

          @if (targetListing()) {
            <div class="listing-preview">
              <strong>{{ targetListing()?.breed || targetListing()?.animalType }}</strong>
              <span><app-icon name="pin" /> {{ targetListing()?.location }}</span>
              <span class="price">KES {{ targetListing()?.fee | number }}</span>
            </div>
          }

          <div class="form-group">
            <label for="inquiryMsg">Introduction Message (Optional)</label>
            <textarea
              id="inquiryMsg"
              rows="3"
              class="form-control"
              [(ngModel)]="inquiryMessage"
              placeholder="e.g. Hello, I am interested in viewing the animal this Saturday. Please contact me..."></textarea>
          </div>

          <div class="btn-row">
            <button
              type="button"
              class="btn-primary"
              [disabled]="isSubmitting()"
              (click)="sendInquiry()">
              {{ isSubmitting() ? 'Sending Inquiry...' : 'Submit Connection Request' }}
            </button>
            <button
              type="button"
              class="btn-secondary"
              (click)="targetListingId.set(null)">
              Cancel
            </button>
          </div>
        </div>
      }

      <!-- Tabs Navigation -->
      <div class="tabs-bar">
        <button
          type="button"
          class="tab-btn"
          [class.active]="activeTab() === 'incoming'"
          (click)="switchTab('incoming')">
          <app-icon name="inbox" /> Incoming Buyer Requests ({{ incomingRequests().length }})
        </button>
        <button
          type="button"
          class="tab-btn"
          [class.active]="activeTab() === 'outgoing'"
          (click)="switchTab('outgoing')">
          <app-icon name="send" /> My Sent Requests ({{ outgoingRequests().length }})
        </button>
      </div>

      <!-- Tab 1: Incoming Buyer Requests -->
      @if (activeTab() === 'incoming') {
        @if (isLoading()) {
          <div class="loading-state">
            <div class="spinner"></div>
            <p>Loading incoming buyer requests...</p>
          </div>
        } @else if (incomingRequests().length === 0) {
          <div class="empty-state">
            <span class="empty-icon"><app-icon name="inbox" /></span>
            <p>No incoming buyer inquiries on your listings yet.</p>
          </div>
        } @else {
          <div class="requests-list">
            @for (req of incomingRequests(); track req.id) {
              <div class="request-item">
                <div class="req-main">
                  <div class="user-meta">
                    <span class="avatar"><app-icon name="user" /></span>
                    <div>
                      <strong>Buyer: &#64;{{ req.interestedUsername || 'Interested Buyer' }}</strong>
                      <span class="date">{{ req.createdAt | date:'medium' }}</span>
                    </div>
                  </div>
                  @if (req.message) {
                    <p class="req-msg">"{{ req.message }}"</p>
                  }
                </div>

                <div class="req-status-actions">
                  <span class="status-badge" [ngClass]="req.status">{{ req.status }}</span>

                  @if (req.status === 'PENDING') {
                    <div class="action-btn-group">
                      <button type="button" class="btn-accept" (click)="acceptRequest(req.id)">
                        Accept
                      </button>
                      <button type="button" class="btn-decline" (click)="declineRequest(req.id)">
                        Decline
                      </button>
                    </div>
                  }
                </div>
              </div>
            }
          </div>
        }
      }

      <!-- Tab 2: Outgoing Sent Requests -->
      @if (activeTab() === 'outgoing') {
        @if (isLoading()) {
          <div class="loading-state">
            <div class="spinner"></div>
            <p>Loading your sent inquiries...</p>
          </div>
        } @else if (outgoingRequests().length === 0) {
          <div class="empty-state">
            <span class="empty-icon"><app-icon name="send" /></span>
            <p>You have not submitted inquiries on any livestock listings yet.</p>
            <a routerLink="/marketplace" class="btn-browse">Browse Livestock Catalog</a>
          </div>
        } @else {
          <div class="requests-list">
            @for (req of outgoingRequests(); track req.id) {
              <div class="request-item">
                <div class="req-main">
                  <div class="user-meta">
                    <span class="avatar"><app-icon name="paw" /></span>
                    <div>
                      <strong>Seller: &#64;{{ req.ownerUsername || 'Listing Owner' }}</strong>
                      <span class="date">{{ req.createdAt | date:'medium' }}</span>
                    </div>
                  </div>
                  @if (req.message) {
                    <p class="req-msg">Your note: "{{ req.message }}"</p>
                  }
                </div>

                <div class="req-status-actions">
                  <span class="status-badge" [ngClass]="req.status">{{ req.status }}</span>
                  @if (req.status === 'ACCEPTED') {
                    <span class="accepted-note"><app-icon name="check" /> Request Accepted! Seller will reach out.</span>
                  }
                </div>
              </div>
            }
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .connections-wrapper {
      max-width: 900px;
      margin: 2rem auto;
      padding: 0 1rem;
    }
    .connections-header {
      margin-bottom: 1.5rem;
      h2 {
        font-size: 1.75rem;
        color: #0f172a;
        margin: 0;
      }
      .subtitle {
        color: #64748b;
        font-size: 0.9rem;
        margin-top: 0.25rem;
      }
    }
    .alert {
      padding: 0.75rem 1rem;
      border-radius: 8px;
      font-size: 0.85rem;
      margin-bottom: 1.25rem;
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
    .inquiry-card {
      background: #f0fdf4;
      border: 1px solid #86efac;
      border-radius: 12px;
      padding: 1.5rem;
      margin-bottom: 2rem;
      .inquiry-header {
        display: flex;
        gap: 0.75rem;
        align-items: flex-start;
        margin-bottom: 1rem;
        .icon {
          font-size: 1.75rem;
        }
        h3 {
          margin: 0;
          color: #166534;
          font-size: 1.15rem;
        }
        p {
          margin: 0.2rem 0 0 0;
          font-size: 0.8rem;
          color: #334155;
        }
      }
      .listing-preview {
        display: flex;
        gap: 1rem;
        align-items: center;
        background: #ffffff;
        padding: 0.6rem 1rem;
        border-radius: 6px;
        margin-bottom: 1rem;
        font-size: 0.85rem;
        .price {
          color: #15803d;
          font-weight: 700;
          margin-left: auto;
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
          background: #ffffff;
        }
      }
      .btn-row {
        display: flex;
        gap: 0.75rem;
      }
    }
    .btn-primary {
      padding: 0.75rem 1.25rem;
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
      background: #ffffff;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
    }
    .tabs-bar {
      display: flex;
      border-bottom: 2px solid #e2e8f0;
      margin-bottom: 1.5rem;
      gap: 1rem;
      .tab-btn {
        padding: 0.75rem 1.25rem;
        border: none;
        background: none;
        font-size: 0.95rem;
        font-weight: 600;
        color: #64748b;
        cursor: pointer;
        position: relative;
        bottom: -2px;
        &.active {
          color: #15803d;
          border-bottom: 3px solid #15803d;
        }
      }
    }
    .loading-state, .empty-state {
      text-align: center;
      padding: 3rem 1rem;
      background: #ffffff;
      border: 1px dashed #cbd5e1;
      border-radius: 12px;
      .empty-icon {
        font-size: 2.5rem;
      }
      p {
        color: #64748b;
        margin: 0.5rem 0 0 0;
      }
      .btn-browse {
        display: inline-block;
        margin-top: 1rem;
        padding: 0.5rem 1rem;
        background: #15803d;
        color: #ffffff;
        border-radius: 6px;
        text-decoration: none;
        font-weight: 700;
        font-size: 0.85rem;
      }
    }
    .spinner {
      width: 36px;
      height: 36px;
      border: 3px solid #e2e8f0;
      border-top-color: #15803d;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin: 0 auto 0.75rem;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    .requests-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .request-item {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      padding: 1.25rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
      @media (max-width: 640px) {
        flex-direction: column;
        align-items: flex-start;
      }
      .req-main {
        .user-meta {
          display: flex;
          align-items: center;
          gap: 0.75rem;
          margin-bottom: 0.4rem;
          .avatar {
            font-size: 1.5rem;
          }
          strong {
            display: block;
            color: #0f172a;
            font-size: 0.95rem;
          }
          .date {
            font-size: 0.75rem;
            color: #94a3b8;
          }
        }
        .req-msg {
          margin: 0.25rem 0 0 0;
          font-size: 0.85rem;
          color: #475569;
          font-style: italic;
        }
      }
      .req-status-actions {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 0.5rem;
        @media (max-width: 640px) {
          align-items: flex-start;
          width: 100%;
          border-top: 1px solid #f1f5f9;
          padding-top: 0.75rem;
        }
        .status-badge {
          padding: 0.25rem 0.6rem;
          border-radius: 20px;
          font-size: 0.75rem;
          font-weight: 700;
          &.PENDING {
            background: #fef9c3;
            color: #854d0e;
          }
          &.ACCEPTED {
            background: #dcfce7;
            color: #166534;
          }
          &.REJECTED {
            background: #fee2e2;
            color: #991b1b;
          }
        }
        .accepted-note {
          font-size: 0.75rem;
          color: #15803d;
          font-weight: 600;
        }
        .action-btn-group {
          display: flex;
          gap: 0.5rem;
          .btn-accept {
            padding: 0.4rem 0.8rem;
            background: #15803d;
            color: #ffffff;
            border: none;
            border-radius: 6px;
            font-weight: 700;
            font-size: 0.8rem;
            cursor: pointer;
            &:hover {
              background: #166534;
            }
          }
          .btn-decline {
            padding: 0.4rem 0.8rem;
            background: #f1f5f9;
            color: #64748b;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            font-size: 0.8rem;
            cursor: pointer;
            &:hover {
              background: #fee2e2;
              color: #991b1b;
            }
          }
        }
      }
    }
  `],
})
export class ConnectionsComponent implements OnInit {
  private readonly connectionService = inject(ConnectionService);
  private readonly listingService = inject(ListingService);
  private readonly route = inject(ActivatedRoute);

  readonly incomingRequests = signal<ConnectionRequestResponse[]>([]);
  readonly outgoingRequests = signal<ConnectionRequestResponse[]>([]);
  readonly targetListingId = signal<string | null>(null);
  readonly targetListing = signal<ListingResponse | null>(null);

  readonly isLoading = signal<boolean>(false);
  readonly isSubmitting = signal<boolean>(false);
  readonly activeTab = signal<'incoming' | 'outgoing'>('incoming');
  readonly statusMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);

  inquiryMessage = '';

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const listingId = params['listingId'];
      if (listingId) {
        this.targetListingId.set(listingId);
        this.listingService.getById(listingId).subscribe({
          next: (l) => this.targetListing.set(l),
          error: () => {},
        });
      }
    });

    this.loadIncoming();
    this.loadOutgoing();
  }

  switchTab(tab: 'incoming' | 'outgoing'): void {
    this.activeTab.set(tab);
    if (tab === 'incoming') this.loadIncoming();
    if (tab === 'outgoing') this.loadOutgoing();
  }

  loadIncoming(): void {
    this.isLoading.set(true);
    this.connectionService.getIncoming().subscribe({
      next: (page) => {
        this.incomingRequests.set(page.content || []);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  loadOutgoing(): void {
    this.isLoading.set(true);
    this.connectionService.getOutgoing().subscribe({
      next: (page) => {
        this.outgoingRequests.set(page.content || []);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  sendInquiry(): void {
    const listingId = this.targetListingId();
    if (!listingId) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.statusMessage.set(null);

    this.connectionService
      .create({
        listingId,
        message: this.inquiryMessage.trim() || undefined,
      })
      .subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.statusMessage.set('Connection request sent to seller successfully.');
          this.targetListingId.set(null);
          this.inquiryMessage = '';
          this.switchTab('outgoing');
        },
        error: (err) => {
          this.isSubmitting.set(false);
          this.errorMessage.set(err.error?.message || 'Failed to submit connection request.');
        },
      });
  }

  acceptRequest(id: string): void {
    this.connectionService.accept(id).subscribe({
      next: () => {
        this.statusMessage.set('Connection request accepted! Phone notification sent to buyer.');
        this.loadIncoming();
      },
      error: (err) => alert(err.error?.message || 'Failed to accept connection request.'),
    });
  }

  declineRequest(id: string): void {
    this.connectionService.reject(id).subscribe({
      next: () => {
        this.statusMessage.set('Connection request declined.');
        this.loadIncoming();
      },
      error: (err) => alert(err.error?.message || 'Failed to decline request.'),
    });
  }
}
