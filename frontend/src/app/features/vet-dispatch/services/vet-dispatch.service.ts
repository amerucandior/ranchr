import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CompleteServiceRequestDto,
  CreateRatingDto,
  CreateServiceRequestDto,
  Page,
  PresenceEvent,
  ServiceRequestResponseDto,
  ServiceStatus,
  VetProfileResponseDto,
  VetRatingResponseDto,
} from '../models/vet-dispatch.models';
import { WebSocketService } from '../../../core/services/websocket.service';

@Injectable({
  providedIn: 'root',
})
export class VetDispatchService {
  private readonly http = inject(HttpClient);
  private readonly ws = inject(WebSocketService);

  createRequest(dto: CreateServiceRequestDto): Observable<ServiceRequestResponseDto> {
    return this.http.post<ServiceRequestResponseDto>('/api/services', dto);
  }

  getServiceById(id: string): Observable<ServiceRequestResponseDto> {
    return this.http.get<ServiceRequestResponseDto>(`/api/services/${id}`);
  }

  listFarmerRequests(page = 0, size = 10): Observable<Page<ServiceRequestResponseDto>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<ServiceRequestResponseDto>>('/api/services/farmer', { params });
  }

  listVetRequests(status?: ServiceStatus, page = 0, size = 10): Observable<Page<ServiceRequestResponseDto>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<Page<ServiceRequestResponseDto>>('/api/services/vet', { params });
  }

  listBroadcasts(location?: string, page = 0, size = 10): Observable<Page<ServiceRequestResponseDto>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    if (location) {
      params = params.set('location', location);
    }
    return this.http.get<Page<ServiceRequestResponseDto>>('/api/services/broadcasts', { params });
  }

  acceptRequest(id: string): Observable<ServiceRequestResponseDto> {
    return this.http.put<ServiceRequestResponseDto>(`/api/services/${id}/accept`, {});
  }

  startService(id: string): Observable<ServiceRequestResponseDto> {
    return this.http.put<ServiceRequestResponseDto>(`/api/services/${id}/start`, {});
  }

  completeService(id: string, dto?: CompleteServiceRequestDto): Observable<ServiceRequestResponseDto> {
    return this.http.put<ServiceRequestResponseDto>(`/api/services/${id}/complete`, dto || {});
  }

  cancelRequest(id: string): Observable<ServiceRequestResponseDto> {
    return this.http.put<ServiceRequestResponseDto>(`/api/services/${id}/cancel`, {});
  }

  rateService(id: string, dto: CreateRatingDto): Observable<VetRatingResponseDto> {
    return this.http.post<VetRatingResponseDto>(`/api/services/${id}/rate`, dto);
  }

  listAvailableVets(): Observable<VetProfileResponseDto[]> {
    const params = new HttpParams()
      .set('available', 'AVAILABLE')
      .set('status', 'APPROVED');
    return this.http.get<VetProfileResponseDto[]>('/vet/vets', { params });
  }

  toggleAvailability(available: 'AVAILABLE' | 'BUSY' | 'UNAVAILABLE'): void {
    const event: PresenceEvent = {
      eventType: available === 'AVAILABLE' ? 'GO_ONLINE' : 'GO_OFFLINE',
      available,
    };
    this.ws.send('/app/vets/availability', event);
  }

  listenToBroadcasts(): Observable<unknown> {
    return this.ws.subscribe('/topic/vets/broadcasts');
  }

  listenToUserNotifications(userId: string): Observable<unknown> {
    return this.ws.subscribe(`/topic/notifications/${userId}`);
  }
}
