import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../../features/marketplace/models/listing.models';

export type ConnectionStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface ConnectionRequestCreateDto {
  listingId: string;
  message?: string;
}

export interface ConnectionRequestResponse {
  id: string;
  listingId: string;
  interestedUserId: string;
  interestedUsername?: string;
  ownerUsername?: string;
  status: ConnectionStatus;
  message?: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class ConnectionService {
  private readonly http = inject(HttpClient);

  create(dto: ConnectionRequestCreateDto): Observable<ConnectionRequestResponse> {
    return this.http.post<ConnectionRequestResponse>('/connections', dto);
  }

  getIncoming(page = 0, size = 20): Observable<PageResponse<ConnectionRequestResponse>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<ConnectionRequestResponse>>('/connections/incoming', { params });
  }

  getOutgoing(page = 0, size = 20): Observable<PageResponse<ConnectionRequestResponse>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<PageResponse<ConnectionRequestResponse>>('/connections/outgoing', { params });
  }

  accept(id: string): Observable<ConnectionRequestResponse> {
    return this.http.put<ConnectionRequestResponse>(`/connections/${id}/accept`, {});
  }

  reject(id: string): Observable<ConnectionRequestResponse> {
    return this.http.put<ConnectionRequestResponse>(`/connections/${id}/reject`, {});
  }
}
