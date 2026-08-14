package com.ranchr.ranchr.vet.model;

import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.vet.model.enums.Available;
import com.ranchr.ranchr.vet.model.enums.ServicesOffered;
import com.ranchr.ranchr.vet.model.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(
        name = "vet_profiles"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetProfile {

    @Id
    @Column(name = "user_id")
    private UUID vetId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User vet;

    @Column(name = "kvb_license_number", nullable = false)
    private String kvbLicenseNumber;
    @Column(name = "kvb_identification_card_front", nullable = false)
    private String kvbIdentificationCardFrontUrl;
    @Column(name = "kvb_identification_card_back", nullable = false)
    private String kvbIdentificationCardBackUrl;
    @Column(name = "kvb_registered_name", nullable = false)
    private String kvbRegisteredName;

    @NotNull
    @Min(0)
    @Max(60)
    @Column(name = "years_experience", nullable = false)
    private Integer yearsExperience;

    @ElementCollection(targetClass = ServicesOffered.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "vet_services",
            joinColumns = @JoinColumn(name = "vet_id")
    )
    @Column(name = "services_offered")
    @Builder.Default
    private Set<ServicesOffered> servicesOffered = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "vetProfile", fetch = FetchType.LAZY)
    private List<ServiceRequest> requests = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Available available = Available.AVAILABLE;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VerificationStatus status = VerificationStatus.UNDER_REVIEW;

    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private String verifiedBy;

}
