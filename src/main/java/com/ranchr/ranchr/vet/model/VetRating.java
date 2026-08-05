package com.ranchr.ranchr.vet.model;

import com.ranchr.authentication.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "vet_ratings"
)
@Getter
@Setter
public class VetRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vet_id", nullable = false)
    private VetProfile vetProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User ratedBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceRequest service;

    @Column(
            nullable = false,
            check = @CheckConstraint(constraint = "rating >= 1 AND rating <= 5"))
    @Max(5)
    @Min(1)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    @Size(max = 250)
    private String comment;

    @CreationTimestamp
    private Instant createdAt;


}
