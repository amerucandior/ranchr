import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { CycleListComponent } from './cycle-list.component';
import { ReproductionService } from '../../services/reproduction.service';
import { of } from 'rxjs';
import { ReproductiveCycleResponseDto } from '../../models/reproduction.models';

describe('CycleListComponent', () => {
  let component: CycleListComponent;
  let reproService: ReproductionService;

  const mockCycle: ReproductiveCycleResponseDto = {
    id: 'cycle-1',
    farmerId: 'farmer-1',
    farmerUsername: 'farmer_kimani',
    animalIdentifier: 'Cow #101',
    animalSpecies: 'Cattle',
    semenStrawBatchNumber: 'KAGRC-9988',
    inseminationDate: '2026-10-01',
    nonReturnCheckDate: '2026-10-22',
    pregnancyDiagnosisDate: '2026-11-30',
    expectedCalvingDate: '2027-07-10',
    status: 'INSEMINATED',
    daysUntilNonReturn: 0,
    daysUntilPregnancyDiagnosis: 17,
    daysUntilCalving: 239,
    day21AlertSent: true,
    day60AlertSent: false,
    calvingAlertSent: false,
    createdAt: '2026-10-01T00:00:00Z',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CycleListComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    reproService = TestBed.inject(ReproductionService);
    vi.spyOn(reproService, 'listFarmerCycles').mockReturnValue(
      of({
        content: [mockCycle],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0,
      })
    );

    const fixture = TestBed.createComponent(CycleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the cycle list component', () => {
    expect(component).toBeTruthy();
  });

  it('should load reproductive cycles on initialization', () => {
    expect(component.cycles().length).toBe(1);
    expect(component.cycles()[0].animalIdentifier).toBe('Cow #101');
  });

  it('should report heat returned when confirmed', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const heatSpy = vi.spyOn(reproService, 'reportHeatReturned').mockReturnValue(of({ ...mockCycle, status: 'HEAT_RETURNED' }));

    component.recordHeatReturned(mockCycle);

    expect(heatSpy).toHaveBeenCalledWith('cycle-1');
  });

  it('should confirm pregnancy when confirmed', () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const confirmSpy = vi.spyOn(reproService, 'confirmPregnancy').mockReturnValue(of({ ...mockCycle, status: 'PREGNANCY_CONFIRMED' }));

    component.confirmPregnancy(mockCycle);

    expect(confirmSpy).toHaveBeenCalledWith('cycle-1');
  });
});
