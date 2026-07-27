package com.ranchr.ranchr.listing.service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ranchr.authentication.model.User;
import com.ranchr.authentication.repository.UserRepository;
import com.ranchr.authentication.security.CurrentUser;
import com.ranchr.cloudinary.service.CloudinaryService;
import com.ranchr.exceptions.ListingAccessDeniedException;
import com.ranchr.exceptions.ListingNotFoundException;
import com.ranchr.ranchr.listing.dto.CreateListingRequest;
import com.ranchr.ranchr.listing.dto.ListingResponse;
import com.ranchr.ranchr.listing.dto.UpdateListingRequest;
import com.ranchr.ranchr.listing.mapper.ListingMapper;
import com.ranchr.ranchr.listing.models.Listing;
import com.ranchr.ranchr.listing.models.ListingMedia;
import com.ranchr.ranchr.listing.models.enums.ListingServiceType;
import com.ranchr.ranchr.listing.models.enums.ListingStatus;
import com.ranchr.ranchr.listing.repository.ListingRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListingService {

	private final ListingRepository listingRepository;
	private final CloudinaryService cloudinaryService;
	private final UserRepository	userRepository;
	private final CurrentUser		currentUser;

	private static final int MAX_FILES = 10;
	private static final int MAX_ANON_LISTINGS = 12;

	public ListingService(ListingRepository listingRepository, CloudinaryService cloudinaryService,
						  UserRepository userRepository, CurrentUser currentUser) {
		this.listingRepository = listingRepository;
		this.cloudinaryService = cloudinaryService;
		this.userRepository		= userRepository;
		this.currentUser		= currentUser;
	}

	@Transactional
	public ListingResponse create(CreateListingRequest request, List<MultipartFile> files) {
		User owner = userRepository.getReferenceById(currentUser.getUserId());
		Listing listing = ListingMapper.toEntity(request, owner);

		if (files != null && !files.isEmpty()) {
			if (files.size() > MAX_FILES) {
				throw new IllegalArgumentException("Too many files: max " + MAX_FILES);
			}
			for (int i = 0; i < files.size(); i++) {
				MultipartFile file = files.get(i);
				String publicId = UuidCreator.getTimeOrderedEpoch().toString();
				Map<String, Object> result = cloudinaryService.uploadFile(file, "listings/" + owner.getUserId(), publicId);

				ListingMedia media = new ListingMedia();
				media.setUrl((String) result.get("secure_url"));
				media.setPublicId(publicId);
				media.setSortOrder(i);
				media.setListing(listing);

				listing.getMedia().add(media);
			}
		}
		Listing saved = listingRepository.save(listing);
		return ListingMapper.toResponse(saved);
	}

	public ListingResponse getById(UUID id) {
		return ListingMapper.toResponse(findOrThrow(id));
	}


	public Page<ListingResponse> search(String animalType, String location,
	                                    ListingServiceType serviceType, ListingStatus status,
	                                    Pageable pageable) {
		if (!currentUser.isAuthenticated()) {
			pageable = PageRequest.of(0, pageable.getPageSize(), Sort.by("createdAt").descending());
		}

		Specification<Listing> spec = (root, query, cb) ->
											  cb.equal(root.get("status"), status != null ? status : ListingStatus.ACTIVE);

		if (animalType != null) {
			spec = spec.and((root, query, cb) ->
									cb.equal(cb.lower(root.get("animalType")), animalType.toLowerCase()));
		}
		if (location != null) {
			spec = spec.and((root, query, cb) ->
									cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
		}
		if (serviceType != null) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("serviceType"), serviceType));
		}

		Page<Listing> page = listingRepository.findAll(spec, pageable);

		List<UUID> ids = page.getContent().stream().map(Listing::getId).toList();

		Map<UUID, Listing> withMedia = listingRepository.findByIdIn(ids).stream()
											   .collect(Collectors.toMap(Listing::getId, l -> l));

		if (!currentUser.isAuthenticated()) {
			List<ListingResponse> truncated = ids.stream()
													  .limit(MAX_ANON_LISTINGS)
													  .map(withMedia::get)
													  .map(ListingMapper::toResponse)
													  .toList();
			return new PageImpl<>(truncated, pageable, truncated.size());
		}

		List<ListingResponse> content = ids.stream()
												.map(withMedia::get)
												.map(ListingMapper::toResponse)
												.toList();

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	@Transactional
	public ListingResponse update(UUID id, UpdateListingRequest request) {
		User owner = userRepository.getReferenceById(currentUser.getUserId());
		Listing listing = findOrThrow(id);
		requireOwnership(listing, owner);
		ListingMapper.applyUpdate(listing, request);
		return ListingMapper.toResponse(listing);
	}

	@Transactional
	public void remove(UUID id) {
		User owner = userRepository.getReferenceById(currentUser.getUserId());
		Listing listing = findOrThrow(id);
		requireOwnership(listing, owner);
		listing.setStatus(ListingStatus.REMOVED);
	}

	@Transactional
	public void recordInterest(UUID id) {
		if (!listingRepository.existsById(id)) {
			throw new ListingNotFoundException(id);
		}
		listingRepository.incrementInterestCount(id);
	}

	private Listing findOrThrow(UUID id) {
		return listingRepository.findById(id).orElseThrow(() -> new ListingNotFoundException(id));
	}

	private void requireOwnership(Listing listing, User currentUser) {
		if (currentUser == null || listing.getOwner() == null
					|| !listing.getOwner().getUserId().equals(currentUser.getUserId())) {
			throw new ListingAccessDeniedException("You do not own this listing");
		}
	}
}