package com.ranchr.ranchr.listing.mapper;

import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.listing.dto.CreateListingRequest;
import com.ranchr.ranchr.listing.dto.ListingMediaResponse;
import com.ranchr.ranchr.listing.dto.ListingResponse;
import com.ranchr.ranchr.listing.dto.UpdateListingRequest;
import com.ranchr.ranchr.listing.models.Listing;
import com.ranchr.ranchr.listing.models.ListingMedia;

import java.util.List;

public final class ListingMapper {

	private ListingMapper() {
	}

	public static Listing toEntity(CreateListingRequest request, User owner) {
		Listing listing = new Listing();
		listing.setOwner(owner);
		listing.setAnimalType(request.animalType());
		listing.setGender(request.gender());
		listing.setAgeYears(request.ageYears());
		listing.setAgeMonths(request.ageMonths());
		listing.setBreed(request.breed());
		listing.setDescription(request.description());
		listing.setLocation(request.location());
		listing.setServiceType(request.serviceType());
		listing.setFee(request.fee());
		listing.setAvailableFrom(request.availableFrom());
		listing.setAvailableTo(request.availableTo());
		return listing;
	}

	/**
	 * Applies a full-replace update onto a managed {@link Listing}. Caller is
	 * expected to be inside a transaction so Hibernate dirty-checking picks
	 * the changes up — no explicit save() needed.
	 */
	public static void applyUpdate(Listing listing, UpdateListingRequest request) {
		listing.setAnimalType(request.animalType());
		listing.setGender(request.gender());
		listing.setAgeYears(request.ageYears());
		listing.setAgeMonths(request.ageMonths());
		listing.setBreed(request.breed());
		listing.setDescription(request.description());
		listing.setLocation(request.location());
		listing.setServiceType(request.serviceType());
		listing.setFee(request.fee());
		listing.setAvailableFrom(request.availableFrom());
		listing.setAvailableTo(request.availableTo());

		if (request.status() != null) {
			listing.setStatus(request.status());
		}
	}

	public static ListingResponse toResponse(Listing listing) {
		List<ListingMediaResponse> images = listing.getMedia().stream()
													.map(ListingMapper::toMediaResponse)
													.toList();
		return new ListingResponse(
				listing.getId(),
				listing.getOwner() != null ? listing.getOwner().getUserId() : null,
				listing.getAnimalType(),
				listing.getGender(),
				listing.getAgeYears(),
				listing.getAgeMonths(),
				listing.getBreed(),
				listing.getDescription(),
				listing.getLocation(),
				listing.getServiceType(),
				listing.getStatus(),
				listing.getFee(),
				listing.getAvailableFrom(),
				listing.getAvailableTo(),
				listing.getInterestCount(),
				images,
				listing.getCreatedAt(),
				listing.getUpdatedAt()
		);
	}


	private static ListingMediaResponse toMediaResponse(ListingMedia m) {
		return new ListingMediaResponse(m.getId(), m.getUrl(), m.getPublicId(), m.getSortOrder());
	}

}