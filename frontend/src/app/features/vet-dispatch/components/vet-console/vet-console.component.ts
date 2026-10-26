import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { VetDispatchService } from '../../services/vet-dispatch.service';
import {
  CompleteServiceRequestDto,
  ServiceRequestResponseDto,
} from '../../models/vet-dispatch.models';

@Component({
  selector: 'app-vet-console',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vet-console.component.html',
  styleUrls: ['./vet-console.component.scss'],
})
export class VetConsoleComponent implements OnInit, OnDestroy {
  private readonly vetService = inject(VetDispatchService);

  readonly availabilityStatus = signal<'AVAILABLE' | 'BUSY' | 'UNAVAILABLE'>('AVAILABLE');
  readonly broadcasts = signal<ServiceRequestResponseDto[]>([]);
  readonly activeDuties = signal<ServiceRequestResponseDto[]>([]);
  readonly isLoading = signal<boolean>(false);
  readonly actionInProgressId = signal<string | null>(null);

  // Completion modal state
  readonly selectedDutyForCompletion = signal<ServiceRequestResponseDto | null>(null);
  semenStrawBatchNumber = '';
  completionNotes = '';

  private broadcastSub: Subscription | null = null;

  ngOnInit(): void {
    this.refreshAll();
    this.subscribeBroadcasts();
  }

  refreshAll(): void {
    this.loadBroadcasts();
    this.loadMyDuties();
  }

  setAvailability(status: 'AVAILABLE' | 'BUSY' | 'UNAVAILABLE'): void {
    this.availabilityStatus.set(status);
    this.vetService.toggleAvailability(status);
  }

  private loadBroadcasts(): void {
    this.vetService.listBroadcasts().subscribe({
      next: (page) => this.broadcasts.set(page.content),
      error: () => console.warn('Could not load broadcasts'),
    });
  }

  private loadMyDuties(): void {
    this.vetService.listVetRequests().subscribe({
      next: (page) => this.activeDuties.set(page.content),
      error: () => console.warn('Could not load assigned duties'),
    });
  }

  private subscribeBroadcasts(): void {
    this.broadcastSub = this.vetService.listenToBroadcasts().subscribe({
      next: () => {
        this.loadBroadcasts();
      },
    });
  }

  acceptBroadcast(req: ServiceRequestResponseDto): void {
    this.actionInProgressId.set(req.serviceId);
    this.vetService.acceptRequest(req.serviceId).subscribe({
      next: () => {
        this.actionInProgressId.set(null);
        this.availabilityStatus.set('BUSY');
        this.refreshAll();
      },
      error: (err) => {
        this.actionInProgressId.set(null);
        alert(err.error?.message || 'Failed to accept dispatch request.');
      },
    });
  }

  startTrip(req: ServiceRequestResponseDto): void {
    this.actionInProgressId.set(req.serviceId);
    this.vetService.startService(req.serviceId).subscribe({
      next: () => {
        this.actionInProgressId.set(null);
        this.refreshAll();
      },
      error: (err) => {
        this.actionInProgressId.set(null);
        alert(err.error?.message || 'Failed to update service status.');
      },
    });
  }

  openCompleteModal(req: ServiceRequestResponseDto): void {
    this.selectedDutyForCompletion.set(req);
    this.semenStrawBatchNumber = 'KAGRC-';
    this.completionNotes = 'Insemination performed successfully under cold-chain compliance.';
  }

  closeCompleteModal(): void {
    this.selectedDutyForCompletion.set(null);
  }

  submitCompletion(): void {
    const duty = this.selectedDutyForCompletion();
    if (!duty) return;

    this.isLoading.set(true);
    const dto: CompleteServiceRequestDto = {
      semenStrawBatchNumber: this.semenStrawBatchNumber.trim(),
      notes: this.completionNotes.trim(),
    };

    this.vetService.completeService(duty.serviceId, dto).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.closeCompleteModal();
        this.availabilityStatus.set('AVAILABLE');
        this.refreshAll();
      },
      error: (err) => {
        this.isLoading.set(false);
        alert(err.error?.message || 'Failed to complete service record.');
      },
    });
  }

  ngOnDestroy(): void {
    if (this.broadcastSub) {
      this.broadcastSub.unsubscribe();
    }
  }
}
