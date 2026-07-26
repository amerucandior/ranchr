package com.ranchr.ranchr.connection.service;

import com.ranchr.authentication.model.User;
import com.ranchr.exceptions.BadRequestException;
import com.ranchr.exceptions.ConflictException;
import com.ranchr.exceptions.ForbiddenException;
import com.ranchr.exceptions.NotFoundException;
import com.ranchr.ranchr.connection.dto.Events;
import com.ranchr.ranchr.connection.models.ConnectionRequest;
import com.ranchr.ranchr.connection.models.enums.ConnectionStatus;
import com.ranchr.ranchr.connection.repository.ConnectionRequestRepository;
import com.ranchr.ranchr.listing.models.Listing;
import com.ranchr.ranchr.listing.models.enums.ListingStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConnectionRequestService {

	private final ConnectionRequestRepository connectionRequestRepository;
	private final ApplicationEventPublisher eventPublisher;

	private static final Set<ConnectionStatus> BLOCKING_STATUSES =
			Set.of(ConnectionStatus.PENDING, ConnectionStatus.ACCEPTED);

	@Transactional
	public ConnectionRequest create(Listing listing, User interestedUser, String message) {
		if (listing.getOwner().equals(interestedUser)) {
			throw new BadRequestException("You cannot request your own listing.");
		}
		if (listing.getStatus() != ListingStatus.ACTIVE) {
			throw new BadRequestException("This listing is not currently active.");
		}
		if (connectionRequestRepository.existsByListingAndInterestedUserAndStatusIn(
				listing, interestedUser, BLOCKING_STATUSES)) {
			throw new ConflictException("You already have an open request for this listing.");
		}

		// Block re-request if the most recent REJECTED request happened in the
		// *current* activation cycle — i.e. owner hasn't reactivated since.
		connectionRequestRepository
				.findTopByListingAndInterestedUserAndStatusOrderByCreatedAtDesc(
						listing, interestedUser, ConnectionStatus.REJECTED)
				.filter(prev -> prev.getListingActivationCycle().equals(listing.getActivationCycle()))
				.ifPresent(prev -> {
					throw new ConflictException(
							"Your previous request was declined. The owner must reactivate the listing before you can request again.");
				});

		ConnectionRequest request = new ConnectionRequest();
		request.setListing(listing);
		request.setInterestedUser(interestedUser);
		request.setMessage(message);
		request.setListingActivationCycle(listing.getActivationCycle());

		try {
			ConnectionRequest saved = connectionRequestRepository.save(request);
			connectionRequestRepository.incrementInterestCount(listing.getId());
			eventPublisher.publishEvent(new Events.ConnectionRequestedEvent(saved.getId()));
			return saved;
		} catch (DataIntegrityViolationException e) {
			throw new ConflictException("You already have an open request for this listing.");
		}
	}

	@Transactional
	public ConnectionRequest accept(UUID requestId, User actingUser) {
		ConnectionRequest request = getOwnedRequestOrThrow(requestId, actingUser);
		requireStatus(request, ConnectionStatus.PENDING);

		request.setStatus(ConnectionStatus.ACCEPTED);
		eventPublisher.publishEvent(new Events.ConnectionApprovedEvent(request.getId()));
		return request;
	}

	@Transactional
	public ConnectionRequest reject(UUID requestId, User actingUser) {
		ConnectionRequest request = getOwnedRequestOrThrow(requestId, actingUser);
		requireStatus(request, ConnectionStatus.PENDING);

		request.setStatus(ConnectionStatus.REJECTED);
		connectionRequestRepository.decrementInterestCount(request.getListing().getId());
		eventPublisher.publishEvent(new Events.ConnectionRejectedEvent(request.getId()));
		return request;
	}

	private ConnectionRequest getOwnedRequestOrThrow(UUID requestId, User actingUser) {
		ConnectionRequest request = connectionRequestRepository.findById(requestId)
											.orElseThrow(() -> new NotFoundException("Connection request not found."));

		if (!request.getListing().getOwner().equals(actingUser)) {
			throw new ForbiddenException("You do not own the listing for this request.");
		}
		return request;
	}

	private void requireStatus(ConnectionRequest request, ConnectionStatus expected) {
		if (request.getStatus() != expected) {
			throw new ConflictException(
					"Request is already " + request.getStatus() + "; cannot transition.");
		}
	}
}