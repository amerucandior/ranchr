package com.ranchr.ranchr.listing.models;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "listing_media")
@ToString(exclude = "listing")
@Data
public class ListingMedia {

	@Id
	private UUID id = UuidCreator.getTimeOrderedEpoch();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "listing_id", nullable = false)
	private Listing listing;

	@Column(nullable = false)
	private String url;

	private String publicId;

	@Column(nullable = false)
	private Integer sortOrder;
}
