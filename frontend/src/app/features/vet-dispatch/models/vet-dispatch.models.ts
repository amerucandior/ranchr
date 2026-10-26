export type ServiceStatus = 'PENDING' | 'ACCEPTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface CreateServiceRequestDto {
  targetVetId?: string | null;
  animalSpecies: string;
  symptoms: string;
  description?: string;
  location: string;
  animalPhotos?: string | null;
  isHeatAlert: boolean;
}

export interface CompleteServiceRequestDto {
  semenStrawBatchNumber?: string;
  notes?: string;
}

export interface ServiceRequestResponseDto {
  serviceId: string;
  farmerId: string;
  farmerUsername: string;
  farmerPhone?: string;
  vetId?: string | null;
  vetName?: string | null;
  vetPhone?: string | null;
  animalSpecies: string;
  symptoms: string;
  description?: string;
  location: string;
  animalPhotos?: string;
  status: ServiceStatus;
  createdAt: string;
  acceptedAt?: string | null;
  completedAt?: string | null;
}

export interface CreateRatingDto {
  rating: number;
  review?: string;
}

export interface VetRatingResponseDto {
  id: string;
  serviceId: string;
  vetId: string;
  vetName: string;
  farmerId: string;
  farmerUsername: string;
  rating: number;
  review?: string;
  createdAt: string;
}

export interface VetProfileResponseDto {
  vetId: string;
  kvbRegisteredName: string;
  available: 'AVAILABLE' | 'BUSY' | 'UNAVAILABLE';
  verificationStatus: 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';
  phone?: string;
  rating?: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface PresenceEvent {
  eventType: 'GO_ONLINE' | 'GO_OFFLINE';
  available: 'AVAILABLE' | 'BUSY' | 'UNAVAILABLE';
}
