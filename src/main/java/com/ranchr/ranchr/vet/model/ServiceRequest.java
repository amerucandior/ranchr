package com.ranchr.ranchr.vet.model;

import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.vet.model.enums.ServiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "service_requests",
        indexes = {
                @Index(name = "idx_service_request_vet", columnList = "vet_id"),
                @Index(name = "idx_service_request_farmer", columnList = "user_id")
        }
)
@Getter
@Setter
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "service_id")
    private UUID serviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id")
    private VetProfile vetProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User farmer;

    private String animalPhotos;
    private String animalSpecies;
    private String symptoms;
    private String location;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private String description;

    @Column(name = "service_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ServiceStatus status = ServiceStatus.PENDING;

    @Version
    private long version;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private Instant acceptedAt;
    private Instant rejectedAt;
    private Instant completedAt;
}
