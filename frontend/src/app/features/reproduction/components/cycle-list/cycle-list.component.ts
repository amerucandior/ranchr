import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ReproductionService } from '../../services/reproduction.service';
import {
  CreateReproductiveCycleDto,
  ReproductiveCycleResponseDto,
} from '../../models/reproduction.models';

@Component({
  selector: 'app-cycle-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cycle-list.component.html',
  styleUrls: ['./cycle-list.component.scss'],
})
export class CycleListComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reproService = inject(ReproductionService);

  readonly cycles = signal<ReproductiveCycleResponseDto[]>([]);
  readonly isLoading = signal<boolean>(true);
  readonly isCreating = signal<boolean>(false);
  readonly actionInProgressId = signal<string | null>(null);

  readonly showCreateModal = signal<boolean>(false);

  cycleForm = this.fb.group({
    animalIdentifier: ['', [Validators.required, Validators.minLength(2)]],
    animalSpecies: ['Cattle', Validators.required],
    semenStrawBatchNumber: ['KAGRC-AYR-2026-'],
    inseminationDate: [new Date().toISOString().split('T')[0], Validators.required],
    notes: ['Inseminated on schedule.'],
  });

  ngOnInit(): void {
    this.loadCycles();
  }

  loadCycles(): void {
    this.isLoading.set(true);
    this.reproService.listFarmerCycles().subscribe({
      next: (page) => {
        this.cycles.set(page.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  submitNewCycle(): void {
    if (this.cycleForm.invalid) {
      this.cycleForm.markAllAsTouched();
      return;
    }

    this.isCreating.set(true);
    const val = this.cycleForm.value;
    const dto: CreateReproductiveCycleDto = {
      animalIdentifier: val.animalIdentifier!,
      animalSpecies: val.animalSpecies!,
      semenStrawBatchNumber: val.semenStrawBatchNumber || undefined,
      inseminationDate: val.inseminationDate!,
      notes: val.notes || undefined,
    };

    this.reproService.createCycle(dto).subscribe({
      next: () => {
        this.isCreating.set(false);
        this.closeCreateModal();
        this.loadCycles();
      },
      error: (err) => {
        this.isCreating.set(false);
        alert(err.error?.message || 'Failed to create reproductive cycle.');
      },
    });
  }

  recordHeatReturned(cycle: ReproductiveCycleResponseDto): void {
    if (confirm(`Confirm animal ${cycle.animalIdentifier} showed signs of heat return around Day 21? This marks non-conception and prepares a new dispatch.`)) {
      this.actionInProgressId.set(cycle.id);
      this.reproService.reportHeatReturned(cycle.id).subscribe({
        next: () => {
          this.actionInProgressId.set(null);
          this.loadCycles();
        },
        error: (err) => {
          this.actionInProgressId.set(null);
          alert(err.error?.message || 'Failed to record heat return.');
        },
      });
    }
  }

  confirmPregnancy(cycle: ReproductiveCycleResponseDto): void {
    if (confirm(`Confirm positive pregnancy diagnosis for ${cycle.animalIdentifier}?`)) {
      this.actionInProgressId.set(cycle.id);
      this.reproService.confirmPregnancy(cycle.id).subscribe({
        next: () => {
          this.actionInProgressId.set(null);
          this.loadCycles();
        },
        error: (err) => {
          this.actionInProgressId.set(null);
          alert(err.error?.message || 'Failed to confirm pregnancy.');
        },
      });
    }
  }

  recordCalved(cycle: ReproductiveCycleResponseDto): void {
    if (confirm(`Confirm successful delivery/calving for ${cycle.animalIdentifier}?`)) {
      this.actionInProgressId.set(cycle.id);
      this.reproService.reportCalved(cycle.id).subscribe({
        next: () => {
          this.actionInProgressId.set(null);
          this.loadCycles();
        },
        error: (err) => {
          this.actionInProgressId.set(null);
          alert(err.error?.message || 'Failed to record calving.');
        },
      });
    }
  }

  triggerSchedulerNow(): void {
    this.reproService.triggerScheduler().subscribe({
      next: (msg) => {
        alert(msg);
        this.loadCycles();
      },
      error: () => alert('Failed to trigger scheduler.'),
    });
  }
}
