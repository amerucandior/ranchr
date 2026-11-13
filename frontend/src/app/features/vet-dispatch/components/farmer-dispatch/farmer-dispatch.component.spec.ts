import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FarmerDispatchComponent } from './farmer-dispatch.component';
import { VetDispatchService } from '../../services/vet-dispatch.service';
import { of } from 'rxjs';
import { ServiceRequestResponseDto } from '../../models/vet-dispatch.models';

describe('FarmerDispatchComponent', () => {
  let component: FarmerDispatchComponent;
  let vetService: VetDispatchService;

  const mockRequest: ServiceRequestResponseDto = {
    serviceId: 'srv-1',
    farmerId: 'farmer-1',
    farmerUsername: 'farmer_dan',
    farmerPhone: '+254711223344',
    animalSpecies: 'Cow / Heifer',
    symptoms: 'Standing heat',
    location: 'Nakuru Farm #4',
    status: 'PENDING',
    createdAt: '2026-10-01T00:00:00Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FarmerDispatchComponent],
      providers: [
        provideRouter([{ path: 'tracker/:id', component: FarmerDispatchComponent }]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    vetService = TestBed.inject(VetDispatchService);
    vi.spyOn(vetService, 'listAvailableVets').mockReturnValue(of([]));
    vi.spyOn(vetService, 'listFarmerRequests').mockReturnValue(
      of({
        content: [mockRequest],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0,
      })
    );
    vi.spyOn(vetService, 'listenToBroadcasts').mockReturnValue(of({} as any));

    const fixture = TestBed.createComponent(FarmerDispatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    component.ngOnDestroy();
  });

  it('should create farmer dispatch component', () => {
    expect(component).toBeTruthy();
  });

  it('should load active requests on init', () => {
    expect(component.activeRequests().length).toBe(1);
    expect(component.activeRequests()[0].animalSpecies).toBe('Cow / Heifer');
  });

  it('should submit heat alert dispatch on valid form submission', () => {
    const createSpy = vi.spyOn(vetService, 'createRequest').mockReturnValue(of(mockRequest));

    component.dispatchForm.patchValue({
      location: 'Njoro Agricultural Ward',
      symptoms: 'Mucus discharge and mounting',
      animalSpecies: 'Cow / Heifer',
      isHeatAlert: true,
    });

    component.submitDispatch();

    expect(createSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        isHeatAlert: true,
        location: 'Njoro Agricultural Ward',
      })
    );
  });
});
