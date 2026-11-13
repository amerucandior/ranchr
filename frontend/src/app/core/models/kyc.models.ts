export interface KycRequest {
  phoneNumber: string;
  nationalId: string;
}

export interface OtpRequest {
  code: string;
}

export interface KycStatus {
  verified: boolean;
  phoneNumber?: string;
  nationalId?: string;
  verifiedAt?: string;
}

export type ServicesOffered =
  | 'ARTIFICIAL_INSEMINATION'
  | 'PREGNANCY_DIAGNOSIS'
  | 'EMERGENCY_OBSTETRICS'
  | 'ROUTINE_CHECKUP'
  | 'VACCINATION'
  | 'SURGERY'
  | 'HERD_HEALTH_CONSULTING';

export type VerificationStatus = 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';
export type VetAvailability = 'AVAILABLE' | 'BUSY' | 'OFFLINE';

export interface CreateVetProfileRequest {
  servicesOffered: ServicesOffered[];
  experienceYears?: number;
}

export interface VetProfileResponse {
  vetId: string;
  kvbRegisteredName: string;
  kvbLicenseNumber: string;
  kvbIdentificationCardFrontUrl: string;
  kvbIdentificationCardBackUrl: string;
  yearsExperience: number;
  servicesOffered: ServicesOffered[];
  status: VerificationStatus;
  available: VetAvailability;
  verified: VerificationStatus;
  verifiedAt?: string;
}
