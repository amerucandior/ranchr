export type ReproductiveStatus =
  | 'PENDING_INSEMINATION'
  | 'INSEMINATED'
  | 'HEAT_RETURNED'
  | 'PREGNANCY_CONFIRMED'
  | 'CALVED'
  | 'FAILED';

export interface CreateReproductiveCycleDto {
  serviceId?: string | null;
  listingId?: string | null;
  vetId?: string | null;
  animalIdentifier: string;
  animalSpecies: string;
  semenStrawBatchNumber?: string;
  inseminationDate?: string;
  notes?: string;
}

export interface ReproductiveCycleResponseDto {
  id: string;
  serviceId?: string | null;
  listingId?: string | null;
  farmerId: string;
  farmerUsername: string;
  vetId?: string | null;
  vetName?: string | null;
  animalIdentifier: string;
  animalSpecies: string;
  semenStrawBatchNumber?: string;
  inseminationDate: string;
  nonReturnCheckDate: string;
  pregnancyDiagnosisDate: string;
  expectedCalvingDate: string;
  status: ReproductiveStatus;
  daysUntilNonReturn: number;
  daysUntilPregnancyDiagnosis: number;
  daysUntilCalving: number;
  day21AlertSent: boolean;
  day60AlertSent: boolean;
  calvingAlertSent: boolean;
  notes?: string;
  createdAt: string;
}
