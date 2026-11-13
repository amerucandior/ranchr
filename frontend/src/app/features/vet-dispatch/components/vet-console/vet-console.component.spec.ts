import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { VetConsoleComponent } from './vet-console.component';
import { VetDispatchService } from '../../services/vet-dispatch.service';
import { of } from 'rxjs';
import { ServiceRequestResponseDto } from '../../models/vet-dispatch.models';

describe('VetConsoleComponent', () => {
  let component: VetConsoleComponent;
  let vetService: VetDispatchService;

  const mockBroadcast: ServiceRequestResponseDto = {
    serviceId: 'srv-101',
    farmerId: 'farmer-101',
    farmerUsername: 'farmer_alice',
    farmerPhone: '+254711998877',
    animalSpecies: 'Friesian Cow',
    symptoms: 'Standing heat',
    location: 'Eldoret North',
    status: 'PENDING',
    createdAt: '2026-10-01T00:00:00Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VetConsoleComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    vetService = TestBed.inject(VetDispatchService);
    vi.spyOn(vetService, 'listBroadcasts').mockReturnValue(
      of({
        content: [mockBroadcast],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0,
      })
    );
    vi.spyOn(vetService, 'listVetRequests').mockReturnValue(
      of({
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 10,
        number: 0,
      })
    );
    vi.spyOn(vetService, 'listenToBroadcasts').mockReturnValue(of({} as any));

    const fixture = TestBed.createComponent(VetConsoleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    component.ngOnDestroy();
  });

  it('should create vet console component', () => {
    expect(component).toBeTruthy();
  });

  it('should load broadcasts on initialization', () => {
    expect(component.broadcasts().length).toBe(1);
    expect(component.broadcasts()[0].serviceId).toBe('srv-101');
  });

  it('should accept broadcast request and refresh duties', () => {
    const acceptSpy = vi.spyOn(vetService, 'acceptRequest').mockReturnValue(
      of({ ...mockBroadcast, status: 'ACCEPTED' })
    );

    component.acceptBroadcast(mockBroadcast);

    expect(acceptSpy).toHaveBeenCalledWith('srv-101');
    expect(component.availabilityStatus()).toBe('BUSY');
  });

  it('should start trip to farm and invoke serviceRequest start', () => {
    const startSpy = vi.spyOn(vetService, 'startService').mockReturnValue(
      of({ ...mockBroadcast, status: 'IN_PROGRESS' })
    );

    component.startTrip(mockBroadcast);

    expect(startSpy).toHaveBeenCalledWith('srv-101');
  });

  it('should complete service with semen straw batch number', () => {
    const completeSpy = vi.spyOn(vetService, 'completeService').mockReturnValue(
      of({ ...mockBroadcast, status: 'COMPLETED' })
    );

    component.openCompleteModal(mockBroadcast);
    component.semenStrawBatchNumber = 'KAGRC-AYR-5544';
    component.completionNotes = 'Inseminated right uterine horn';

    component.submitCompletion();

    expect(completeSpy).toHaveBeenCalledWith('srv-101', {
      semenStrawBatchNumber: 'KAGRC-AYR-5544',
      notes: 'Inseminated right uterine horn',
    });
  });
});
