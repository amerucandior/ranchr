package com.ranchr.ranchr.connection.models;

import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.connection.models.enums.ConnectionStatus;
import com.ranchr.ranchr.listing.models.Listing;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
		name = "connection_requests",
		indexes = {
				@Index(name = "idx_connreq_listing", columnList = "listing_id"),
				@Index(name = "idx_connreq_user", columnList = "interested_user_id")
		}
)
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"listing", "interestedUser"})
public class ConnectionRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@EqualsAndHashCode.Include
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "listing_id", nullable = false)
	private Listing listing;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "interested_user_id", nullable = false)
	private User interestedUser;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private ConnectionStatus status = ConnectionStatus.PENDING;

	@Column(columnDefinition = "TEXT")
	private String message;

	@Column(nullable = false)
	private Integer listingActivationCycle;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(nullable = false)
	private Instant updatedAt;

}
