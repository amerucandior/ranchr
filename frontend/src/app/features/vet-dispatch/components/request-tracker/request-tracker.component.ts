import { Component, OnInit, OnDestroy, inject, signal, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { VetDispatchService } from '../../services/vet-dispatch.service';
import {
  CreateRatingDto,
  ServiceRequestResponseDto,
  ServiceStatus,
} from '../../models/vet-dispatch.models';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-request-tracker',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './request-tracker.component.html',
  styleUrls: ['./request-tracker.component.scss'],
})
export class RequestTrackerComponent implements OnInit, OnDestroy {
  private readonly vetService = inject(VetDispatchService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // Router input binding from :id
  readonly id = input.required<string>();

  readonly request = signal<ServiceRequestResponseDto | null>(null);
  readonly isLoading = signal<boolean>(true);
  readonly errorMessage = signal<string | null>(null);

  // Rating modal / submission state
  ratingStars = 5;
  ratingReview = '';
  readonly isSubmittingRating = signal<boolean>(false);
  readonly ratingSubmitted = signal<boolean>(false);

  private wsSub: Subscription | null = null;
  private pollInterval: ReturnType<typeof setInterval> | null = null;

  readonly steps: { status: ServiceStatus; label: string; desc: string }[] = [
    {
      status: 'PENDING',
      label: 'Broadcast Dispatched',
      desc: 'Alert sent to all certified KVB officers in vicinity',
    },
    {
      status: 'ACCEPTED',
      label: 'Vet Assigned',
      desc: 'Practitioner confirmed and preparing liquid nitrogen tank',
    },
    {
      status: 'IN_PROGRESS',
      label: 'En Route / On Farm',
      desc: 'Officer traveling by motorcycle to your coordinates',
    },
    {
      status: 'COMPLETED',
      label: 'Insemination Complete',
      desc: 'Semen straw batch logged & 21-day calendar activated',
    },
  ];

  ngOnInit(): void {
    this.loadRequestDetails();
    this.startTracking();
  }

  loadRequestDetails(): void {
    this.vetService.getServiceById(this.id()).subscribe({
      next: (data) => {
        this.request.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || 'Could not load service request details.');
      },
    });
  }

  private startTracking(): void {
    // Poll every 5s for fallback and subscribe to push
    this.pollInterval = setInterval(() => {
      this.loadRequestDetails();
    }, 5000);

    const user = this.authService.currentUser();
    if (user) {
      this.wsSub = this.vetService.listenToBroadcasts().subscribe({
        next: () => this.loadRequestDetails(),
      });
    }
  }

  isStepCompleted(stepStatus: ServiceStatus): boolean {
    const req = this.request();
    if (!req) return false;

    const order: ServiceStatus[] = ['PENDING', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED'];
    const currentIndex = order.indexOf(req.status);
    const stepIndex = order.indexOf(stepStatus);

    return currentIndex >= stepIndex && req.status !== 'CANCELLED';
  }

  isStepActive(stepStatus: ServiceStatus): boolean {
    const req = this.request();
    return req?.status === stepStatus;
  }

  submitRating(): void {
    const req = this.request();
    if (!req) return;

    this.isSubmittingRating.set(true);
    const dto: CreateRatingDto = {
      rating: this.ratingStars,
      review: this.ratingReview.trim() || undefined,
    };

    this.vetService.rateService(req.serviceId, dto).subscribe({
      next: () => {
        this.isSubmittingRating.set(false);
        this.ratingSubmitted.set(true);
      },
      error: (err) => {
        this.isSubmittingRating.set(false);
        alert(err.error?.message || 'Failed to submit rating.');
      },
    });
  }

  cancelRequest(): void {
    const req = this.request();
    if (!req) return;

    if (confirm('Are you sure you want to cancel this dispatch request?')) {
      this.vetService.cancelRequest(req.serviceId).subscribe({
        next: () => {
          this.loadRequestDetails();
        },
        error: (err) => alert(err.error?.message || 'Could not cancel request.'),
      });
    }
  }

  ngOnDestroy(): void {
    if (this.pollInterval) clearInterval(this.pollInterval);
    if (this.wsSub) this.wsSub.unsubscribe();
  }
}
