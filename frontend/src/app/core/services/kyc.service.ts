import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateVetProfileRequest,
  KycRequest,
  KycStatus,
  OtpRequest,
  VetProfileResponse,
} from '../models/kyc.models';

@Injectable({
  providedIn: 'root',
})
export class KycService {
  private readonly http = inject(HttpClient);

  requestOtp(payload: KycRequest): Observable<string> {
    return this.http.post('/kyc/otprequest', payload, { responseType: 'text' });
  }

  verifyOtp(payload: OtpRequest): Observable<string> {
    return this.http.post('/kyc/verify', payload, { responseType: 'text' });
  }

  getKycStatus(): Observable<KycStatus> {
    return this.http.get<KycStatus>('/kyc/status');
  }

  enlistVetProfile(payload: CreateVetProfileRequest): Observable<VetProfileResponse> {
    return this.http.post<VetProfileResponse>('/vet/enlist', payload);
  }

  updateKvbVerification(formData: FormData): Observable<VetProfileResponse> {
    return this.http.put<VetProfileResponse>('/vet/vets/update-kvb-verification', formData);
  }

  getVetProfile(vetId: string): Observable<VetProfileResponse> {
    return this.http.get<VetProfileResponse>(`/vet/vets/${vetId}`);
  }
}
