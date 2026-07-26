package com.ranchr.ranchr.listing.repository;

import com.ranchr.ranchr.listing.models.Listing;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID>,
												   JpaSpecificationExecutor<Listing> {

	@Modifying
	@Query("UPDATE Listing l SET l.interestCount = l.interestCount + 1 WHERE l.id = :listingId")
	void incrementInterestCount(@Param("listingId") UUID listingId);

	@EntityGraph(attributePaths = "media")
	List<Listing> findByIdIn(List<UUID> ids);
}