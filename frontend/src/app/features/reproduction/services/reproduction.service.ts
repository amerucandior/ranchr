import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateReproductiveCycleDto,
  ReproductiveCycleResponseDto,
} from '../models/reproduction.models';
import { Page } from '../../vet-dispatch/models/vet-dispatch.models';

@Injectable({
  providedIn: 'root',
})
export class ReproductionService {
  private readonly http = inject(HttpClient);

  createCycle(dto: CreateReproductiveCycleDto): Observable<ReproductiveCycleResponseDto> {
    return this.http.post<ReproductiveCycleResponseDto>('/api/reproduction/cycles', dto);
  }

  listFarmerCycles(page = 0, size = 15): Observable<Page<ReproductiveCycleResponseDto>> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());
    return this.http.get<Page<ReproductiveCycleResponseDto>>('/api/reproduction/cycles', { params });
  }

  getCycleById(id: string): Observable<ReproductiveCycleResponseDto> {
    return this.http.get<ReproductiveCycleResponseDto>(`/api/reproduction/cycles/${id}`);
  }

  confirmPregnancy(id: string): Observable<ReproductiveCycleResponseDto> {
    return this.http.put<ReproductiveCycleResponseDto>(`/api/reproduction/cycles/${id}/confirm-pregnancy`, {});
  }

  reportHeatReturned(id: string): Observable<ReproductiveCycleResponseDto> {
    return this.http.put<ReproductiveCycleResponseDto>(`/api/reproduction/cycles/${id}/report-heat`, {});
  }

  reportCalved(id: string): Observable<ReproductiveCycleResponseDto> {
    return this.http.put<ReproductiveCycleResponseDto>(`/api/reproduction/cycles/${id}/calve`, {});
  }

  triggerScheduler(): Observable<string> {
    return this.http.post<string>('/api/reproduction/admin/trigger-scheduler', {}, { responseType: 'text' as 'json' });
  }
}
