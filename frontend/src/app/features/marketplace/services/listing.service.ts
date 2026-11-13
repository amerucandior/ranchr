import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateListingRequest,
  ListingResponse,
  ListingSearchParams,
  PageResponse,
} from '../models/listing.models';

@Injectable({
  providedIn: 'root',
})
export class ListingService {
  private readonly http = inject(HttpClient);

  search(params: ListingSearchParams = {}): Observable<PageResponse<ListingResponse>> {
    let httpParams = new HttpParams();
    if (params.animalType) httpParams = httpParams.set('animalType', params.animalType);
    if (params.location) httpParams = httpParams.set('location', params.location);
    if (params.serviceType) httpParams = httpParams.set('serviceType', params.serviceType);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());

    return this.http.get<PageResponse<ListingResponse>>('/api/listings', { params: httpParams });
  }

  getById(id: string): Observable<ListingResponse> {
    return this.http.get<ListingResponse>(`/api/listings/${id}`);
  }

  create(request: CreateListingRequest, files: File[] = []): Observable<ListingResponse> {
    const formData = new FormData();
    const listingBlob = new Blob([JSON.stringify(request)], { type: 'application/json' });
    formData.append('listing', listingBlob);

    for (const file of files) {
      formData.append('files', file);
    }

    return this.http.post<ListingResponse>('/api/listings/upload', formData);
  }

  recordInterest(id: string): Observable<void> {
    return this.http.post<void>(`/api/listings/${id}/interest`, {});
  }
}
