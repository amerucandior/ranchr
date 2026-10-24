package com.ranchr.ranchr.reproduction.model;

import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.listing.models.Listing;
import com.ranchr.ranchr.reproduction.model.enums.ReproductiveStatus;
import com.ranchr.ranchr.vet.model.ServiceRequest;
import com.ranchr.ranchr.vet.model.VetProfile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "reproductive_cycles",
        indexes = {
                @Index(name = "idx_rep_farmer", columnList = "user_id"),
                @Index(name = "idx_rep_status", columnList = "status"),
                @Index(name = "idx_rep_day21", columnList = "non_return_check_date"),
                @Index(name = "idx_rep_day60", columnList = "pregnancy_diagnosis_date"),
                @Index(name = "idx_rep_calving", columnList = "expected_calving_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReproductiveCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cycle_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User farmer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id")
    private VetProfile vetProfile;

    @Column(name = "animal_identifier", nullable = false)
    private String animalIdentifier;

    @Column(name = "animal_species", nullable = false)
    private String animalSpecies;

    @Column(name = "semen_straw_batch_number")
    private String semenStrawBatchNumber;

    @Column(name = "insemination_date", nullable = false)
    private LocalDate inseminationDate;

    @Column(name = "non_return_check_date", nullable = false)
    private LocalDate nonReturnCheckDate;

    @Column(name = "pregnancy_diagnosis_date", nullable = false)
    private LocalDate pregnancyDiagnosisDate;

    @Column(name = "expected_calving_date", nullable = false)
    private LocalDate expectedCalvingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReproductiveStatus status = ReproductiveStatus.INSEMINATED;

    @Column(name = "day21_alert_sent", nullable = false)
    @Builder.Default
    private boolean day21AlertSent = false;

    @Column(name = "day60_alert_sent", nullable = false)
    @Builder.Default
    private boolean day60AlertSent = false;

    @Column(name = "calving_alert_sent", nullable = false)
    @Builder.Default
    private boolean calvingAlertSent = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
