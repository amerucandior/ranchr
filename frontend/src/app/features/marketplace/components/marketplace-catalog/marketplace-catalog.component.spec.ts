import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MarketplaceCatalogComponent } from './marketplace-catalog.component';
import { ListingService } from '../../services/listing.service';
import { of } from 'rxjs';
import { ListingResponse } from '../../models/listing.models';

describe('MarketplaceCatalogComponent', () => {
  let component: MarketplaceCatalogComponent;
  let listingService: ListingService;

  const mockListings: ListingResponse[] = [
    {
      id: 'list-1',
      ownerId: 'owner-1',
      animalType: 'Cow',
      breed: 'Friesian Champion',
      gender: 'FEMALE',
      location: 'Nakuru',
      serviceType: 'ARTIFICIAL_INSEMINATION',
      status: 'ACTIVE',
      fee: 80000,
      interestCount: 5,
      images: [],
      createdAt: '2026-10-01T00:00:00Z',
      updatedAt: '2026-10-01T00:00:00Z',
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MarketplaceCatalogComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    listingService = TestBed.inject(ListingService);
    vi.spyOn(listingService, 'search').mockReturnValue(
      of({
        content: mockListings,
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0,
      })
    );

    const fixture = TestBed.createComponent(MarketplaceCatalogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create marketplace catalog component', () => {
    expect(component).toBeTruthy();
  });

  it('should load listings on initialization', () => {
    expect(component.listings().length).toBe(1);
    expect(component.listings()[0].breed).toBe('Friesian Champion');
  });

  it('should filter by service type when chip selected', () => {
    const searchSpy = vi.spyOn(listingService, 'search');

    component.selectServiceType('NATURAL');

    expect(component.selectedServiceType).toBe('NATURAL');
    expect(searchSpy).toHaveBeenCalledWith(
      expect.objectContaining({ serviceType: 'NATURAL' })
    );
  });

  it('should increment interest count when expressInterest is called', () => {
    vi.spyOn(listingService, 'recordInterest').mockReturnValue(of(undefined));

    const item = { ...mockListings[0] };
    component.expressInterest(item);

    expect(item.interestCount).toBe(6);
  });
});
