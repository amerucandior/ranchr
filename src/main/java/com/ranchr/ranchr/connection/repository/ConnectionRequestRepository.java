package com.ranchr.ranchr.connection.repository;

import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.connection.models.ConnectionRequest;
import com.ranchr.ranchr.connection.models.enums.ConnectionStatus;
import com.ranchr.ranchr.listing.models.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, UUID> {

	Page<ConnectionRequest> findByListing_Owner(User owner, Pageable pageable);

	Page<ConnectionRequest> findByInterestedUser(User user, Pageable pageable);

	boolean existsByListingAndInterestedUserAndStatusIn(
			Listing listing, User interestedUser, Collection<ConnectionStatus> statuses);

	Optional<ConnectionRequest> findTopByListingAndInterestedUserAndStatusOrderByCreatedAtDesc(
			Listing listing, User interestedUser, ConnectionStatus status);

	@Modifying
	@Query("UPDATE Listing l SET l.interestCount = l.interestCount + 1 WHERE l.id = :id")
	void incrementInterestCount(@Param("id") UUID id);

	@Modifying
	@Query("UPDATE Listing l SET l.interestCount = GREATEST(l.interestCount - 1, 0) WHERE l.id = :id")
	void decrementInterestCount(@Param("id") UUID id);
}