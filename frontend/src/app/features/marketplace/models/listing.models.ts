export type Gender = 'MALE' | 'FEMALE';
export type ListingServiceType = 'NATURAL' | 'ARTIFICIAL_INSEMINATION' | 'BOTH';
export type ListingStatus = 'ACTIVE' | 'INACTIVE' | 'EXPIRED' | 'REMOVED';

export interface ListingMediaResponse {
  id: string;
  url: string;
  publicId: string;
  sortOrder?: number;
}

export interface ListingResponse {
  id: string;
  ownerId: string;
  animalType: string;
  gender?: Gender;
  ageYears?: number;
  ageMonths?: number;
  breed?: string;
  description?: string;
  location: string;
  serviceType: ListingServiceType;
  status: ListingStatus;
  fee: number;
  availableFrom?: string;
  availableTo?: string;
  interestCount: number;
  images: ListingMediaResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateListingRequest {
  animalType: string;
  gender?: Gender;
  ageYears?: number;
  ageMonths?: number;
  breed?: string;
  description?: string;
  location: string;
  serviceType: ListingServiceType;
  fee: number;
  availableFrom?: string;
  availableTo?: string;
}

export interface ListingSearchParams {
  animalType?: string;
  location?: string;
  serviceType?: ListingServiceType;
  status?: ListingStatus;
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
