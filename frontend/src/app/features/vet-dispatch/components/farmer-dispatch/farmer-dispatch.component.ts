import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { VetDispatchService } from '../../services/vet-dispatch.service';
import {
  CreateServiceRequestDto,
  ServiceRequestResponseDto,
  VetProfileResponseDto,
} from '../../models/vet-dispatch.models';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-farmer-dispatch',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './farmer-dispatch.component.html',
  styleUrls: ['./farmer-dispatch.component.scss'],
})
export class FarmerDispatchComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly vetService = inject(VetDispatchService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly availableVets = signal<VetProfileResponseDto[]>([]);
  readonly activeRequests = signal<ServiceRequestResponseDto[]>([]);
  readonly isSubmitting = signal<boolean>(false);
  readonly successMessage = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);

  // 12-Hour Heat Window countdown timer
  readonly hoursRemaining = signal<number>(12);
  readonly minutesRemaining = signal<number>(0);
  readonly secondsRemaining = signal<number>(0);

  private timerInterval: ReturnType<typeof setInterval> | null = null;
  private wsSubscription: Subscription | null = null;

  dispatchForm = this.fb.group({
    isHeatAlert: [true],
    animalSpecies: ['Cow / Heifer', Validators.required],
    symptoms: ['Standing heat observed; mounting behavior and clear mucus discharge', Validators.required],
    description: ['Urgent insemination required. Liquid nitrogen straws preferred.'],
    location: ['', [Validators.required, Validators.minLength(5)]],
    targetVetId: [''],
  });

  ngOnInit(): void {
    this.startHeatClock();
    this.loadAvailableVets();
    this.loadMyRequests();
    this.subscribeToRealtimeUpdates();
  }

  private startHeatClock(): void {
    let totalSeconds = 12 * 3600;
    this.timerInterval = setInterval(() => {
      if (totalSeconds > 0) {
        totalSeconds--;
        this.hoursRemaining.set(Math.floor(totalSeconds / 3600));
        this.minutesRemaining.set(Math.floor((totalSeconds % 3600) / 60));
        this.secondsRemaining.set(totalSeconds % 60);
      }
    }, 1000);
  }

  private loadAvailableVets(): void {
    this.vetService.listAvailableVets().subscribe({
      next: (vets) => this.availableVets.set(vets),
      error: () => console.warn('Could not load online vets list'),
    });
  }

  private loadMyRequests(): void {
    this.vetService.listFarmerRequests(0, 10).subscribe({
      next: (page) => this.activeRequests.set(page.content),
      error: () => console.warn('Could not load farmer requests'),
    });
  }

  private subscribeToRealtimeUpdates(): void {
    const user = this.authService.currentUser();
    if (!user) return;

    this.wsSubscription = this.vetService.listenToBroadcasts().subscribe({
      next: () => this.loadMyRequests(),
    });
  }

  submitDispatch(): void {
    if (this.dispatchForm.invalid) {
      this.dispatchForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const val = this.dispatchForm.value;
    const dto: CreateServiceRequestDto = {
      isHeatAlert: !!val.isHeatAlert,
      animalSpecies: val.animalSpecies!,
      symptoms: val.symptoms!,
      description: val.description || undefined,
      location: val.location!,
      targetVetId: val.targetVetId ? val.targetVetId : null,
    };

    this.vetService.createRequest(dto).subscribe({
      next: (res) => {
        this.isSubmitting.set(false);
        this.successMessage.set(
          val.isHeatAlert
            ? 'Heat alert broadcast sent to all nearby KVB veterinary officers via SMS and WebSocket!'
            : 'Service request submitted successfully!'
        );
        this.loadMyRequests();
        this.dispatchForm.patchValue({
          location: '',
          targetVetId: '',
        });
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(
          err.error?.message || 'Failed to dispatch service request. Please check inputs and retry.'
        );
      },
    });
  }

  selectSpecies(species: string): void {
    this.dispatchForm.patchValue({ animalSpecies: species });
  }

  toggleBroadcast(isBroadcast: boolean): void {
    if (isBroadcast) {
      this.dispatchForm.patchValue({ targetVetId: '' });
    }
  }

  ngOnDestroy(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
  }
}
