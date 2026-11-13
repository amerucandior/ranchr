package com.ranchr.ranchr.listing.service;

import com.ranchr.authentication.model.User;
import com.ranchr.authentication.model.enums.Role;
import com.ranchr.authentication.repository.UserRepository;
import com.ranchr.authentication.security.CurrentUser;
import com.ranchr.cloudinary.service.CloudinaryService;
import com.ranchr.exceptions.ListingAccessDeniedException;
import com.ranchr.exceptions.ListingNotFoundException;
import com.ranchr.ranchr.listing.dto.CreateListingRequest;
import com.ranchr.ranchr.listing.dto.ListingResponse;
import com.ranchr.ranchr.listing.dto.UpdateListingRequest;
import com.ranchr.ranchr.listing.models.Listing;
import com.ranchr.ranchr.listing.models.enums.Gender;
import com.ranchr.ranchr.listing.models.enums.ListingServiceType;
import com.ranchr.ranchr.listing.models.enums.ListingStatus;
import com.ranchr.ranchr.listing.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private ListingService listingService;

    private UUID ownerId;
    private User owner;
    private User otherUser;
    private Listing listing;
    private UUID listingId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        listingId = UUID.randomUUID();

        owner = User.builder()
                .userId(ownerId)
                .username("farmer_dan")
                .email("dan@example.com")
                .role(Role.USER)
                .build();

        otherUser = User.builder()
                .userId(UUID.randomUUID())
                .username("stranger")
                .email("stranger@example.com")
                .role(Role.USER)
                .build();

        listing = new Listing();
        listing.setId(listingId);
        listing.setOwner(owner);
        listing.setAnimalType("Cow");
        listing.setBreed("Friesian");
        listing.setLocation("Nakuru");
        listing.setServiceType(ListingServiceType.ARTIFICIAL_INSEMINATION);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setFee(BigDecimal.valueOf(85000));
        listing.setMedia(new ArrayList<>());
    }

    @Test
    void create_ShouldPersistListingWithUploadedMedia_WhenFilesProvided() {
        CreateListingRequest request = new CreateListingRequest(
                "Cow", Gender.FEMALE, 3, 2, "Friesian", "High yield dairy cow",
                "Nakuru", ListingServiceType.ARTIFICIAL_INSEMINATION, BigDecimal.valueOf(85000),
                LocalDate.now(), LocalDate.now().plusMonths(3)
        );

        MockMultipartFile file = new MockMultipartFile("files", "cow.jpg", "image/jpeg", "image_bytes".getBytes());
        List<MultipartFile> files = List.of(file);

        when(currentUser.getUserId()).thenReturn(ownerId);
        when(userRepository.getReferenceById(ownerId)).thenReturn(owner);
        when(cloudinaryService.uploadFile(any(), any(), any())).thenReturn(Map.of("secure_url", "https://cloudinary.com/cow.jpg"));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ListingResponse response = listingService.create(request, files);

        assertNotNull(response);
        assertEquals("Cow", response.animalType());
        assertEquals("Friesian", response.breed());
        verify(cloudinaryService).uploadFile(eq(file), contains("listings/" + ownerId), any());
        verify(listingRepository).save(any(Listing.class));
    }

    @Test
    void create_ShouldThrowIllegalArgumentException_WhenFilesExceedLimit() {
        CreateListingRequest request = new CreateListingRequest(
                "Cow", Gender.FEMALE, 3, 2, "Friesian", "Desc",
                "Nakuru", ListingServiceType.ARTIFICIAL_INSEMINATION, BigDecimal.valueOf(85000),
                null, null
        );

        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            files.add(new MockMultipartFile("files", "img" + i + ".jpg", "image/jpeg", new byte[10]));
        }

        when(currentUser.getUserId()).thenReturn(ownerId);
        when(userRepository.getReferenceById(ownerId)).thenReturn(owner);

        assertThrows(IllegalArgumentException.class, () -> listingService.create(request, files));
    }

    @Test
    void getById_ShouldReturnListing_WhenExists() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        ListingResponse response = listingService.getById(listingId);

        assertNotNull(response);
        assertEquals(listingId, response.id());
        assertEquals("Nakuru", response.location());
    }

    @Test
    void getById_ShouldThrowListingNotFoundException_WhenMissing() {
        when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        assertThrows(ListingNotFoundException.class, () -> listingService.getById(listingId));
    }

    @Test
    void search_ShouldReturnActiveListings_WhenQueriedByAuthenticatedUser() {
        when(currentUser.isAuthenticated()).thenReturn(true);
        when(listingRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(listing)));
        when(listingRepository.findByIdIn(List.of(listingId))).thenReturn(List.of(listing));

        Page<ListingResponse> page = listingService.search(
                "cow", "Nakuru", ListingServiceType.ARTIFICIAL_INSEMINATION, ListingStatus.ACTIVE, PageRequest.of(0, 10));

        assertNotNull(page);
        assertEquals(1, page.getTotalElements());
        assertEquals("Friesian", page.getContent().get(0).breed());
    }

    @Test
    void update_ShouldUpdateListingFields_WhenOwner() {
        UpdateListingRequest request = new UpdateListingRequest(
                "Cow", Gender.FEMALE, 4, 0, "Friesian High Grade", "Updated desc",
                "Naivasha", ListingServiceType.ARTIFICIAL_INSEMINATION, ListingStatus.ACTIVE,
                BigDecimal.valueOf(95000), null, null
        );

        when(currentUser.getUserId()).thenReturn(ownerId);
        when(userRepository.getReferenceById(ownerId)).thenReturn(owner);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        ListingResponse response = listingService.update(listingId, request);

        assertNotNull(response);
        assertEquals("Naivasha", listing.getLocation());
        assertEquals(BigDecimal.valueOf(95000), listing.getFee());
    }

    @Test
    void update_ShouldThrowListingAccessDeniedException_WhenNotOwner() {
        UpdateListingRequest request = new UpdateListingRequest(
                "Cow", Gender.FEMALE, 4, 0, "Friesian", "Desc",
                "Naivasha", ListingServiceType.ARTIFICIAL_INSEMINATION, ListingStatus.ACTIVE,
                BigDecimal.valueOf(95000), null, null
        );

        UUID strangerId = UUID.randomUUID();
        when(currentUser.getUserId()).thenReturn(strangerId);
        when(userRepository.getReferenceById(strangerId)).thenReturn(otherUser);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        assertThrows(ListingAccessDeniedException.class, () -> listingService.update(listingId, request));
    }

    @Test
    void remove_ShouldSetStatusRemoved_WhenOwner() {
        when(currentUser.getUserId()).thenReturn(ownerId);
        when(userRepository.getReferenceById(ownerId)).thenReturn(owner);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        listingService.remove(listingId);

        assertEquals(ListingStatus.REMOVED, listing.getStatus());
    }

    @Test
    void recordInterest_ShouldIncrementCount_WhenExists() {
        when(listingRepository.existsById(listingId)).thenReturn(true);

        listingService.recordInterest(listingId);

        verify(listingRepository).incrementInterestCount(listingId);
    }
}
