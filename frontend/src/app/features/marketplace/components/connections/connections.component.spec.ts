import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ConnectionsComponent } from './connections.component';
import { ConnectionService, ConnectionRequestResponse } from '../../../../core/services/connection.service';
import { of } from 'rxjs';

describe('ConnectionsComponent', () => {
  let component: ConnectionsComponent;
  let connectionService: ConnectionService;

  const mockIncoming: ConnectionRequestResponse[] = [
    {
      id: 'conn-1',
      listingId: 'list-1',
      interestedUserId: 'user-buyer',
      interestedUsername: 'buyer_mary',
      ownerUsername: 'farmer_john',
      status: 'PENDING',
      message: 'Can I view this heifer?',
      createdAt: '2026-10-15T00:00:00Z',
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConnectionsComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    connectionService = TestBed.inject(ConnectionService);
    vi.spyOn(connectionService, 'getIncoming').mockReturnValue(
      of({
        content: mockIncoming,
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0,
      })
    );
    vi.spyOn(connectionService, 'getOutgoing').mockReturnValue(
      of({
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: 10,
        number: 0,
      })
    );

    const fixture = TestBed.createComponent(ConnectionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create connections component', () => {
    expect(component).toBeTruthy();
  });

  it('should load incoming requests on init', () => {
    expect(component.incomingRequests().length).toBe(1);
    expect(component.incomingRequests()[0].interestedUsername).toBe('buyer_mary');
  });

  it('should call accept when acceptRequest invoked', () => {
    const acceptSpy = vi.spyOn(connectionService, 'accept').mockReturnValue(
      of({ ...mockIncoming[0], status: 'ACCEPTED' })
    );

    component.acceptRequest('conn-1');

    expect(acceptSpy).toHaveBeenCalledWith('conn-1');
  });

  it('should call reject when declineRequest invoked', () => {
    const rejectSpy = vi.spyOn(connectionService, 'reject').mockReturnValue(
      of({ ...mockIncoming[0], status: 'REJECTED' })
    );

    component.declineRequest('conn-1');

    expect(rejectSpy).toHaveBeenCalledWith('conn-1');
  });
});
