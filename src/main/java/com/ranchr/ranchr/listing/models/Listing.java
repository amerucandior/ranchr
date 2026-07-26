package com.ranchr.ranchr.listing.models;

import com.github.f4b6a3.uuid.UuidCreator;
import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.listing.models.enums.Gender;
import com.ranchr.ranchr.listing.models.enums.ListingServiceType;
import com.ranchr.ranchr.listing.models.enums.ListingStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "listings")
@Data
public class Listing {

	@Id
	@Column(name = "listing_id")
	private UUID id = UuidCreator.getTimeOrderedEpoch();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(name = "animal_type", nullable = false)
	private String animalType;

	private Gender gender;

	private Integer ageYears;
	private Integer ageMonths;

	private String breed;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private String location;


	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ListingServiceType serviceType;


	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ListingStatus status = ListingStatus.ACTIVE;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal fee;

	private LocalDate availableFrom;
	private LocalDate availableTo;

	@Column(name = "interest_count", nullable = false)
	private int interestCount = 0;

	@Column(nullable = false)
	private Integer activationCycle = 0;

	@OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC")
	private List<ListingMedia> media = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
