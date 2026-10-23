package com.ranchr.ranchr.vet.repository;

import com.ranchr.ranchr.vet.model.VetProfile;
import com.ranchr.ranchr.vet.model.VetRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VetRatingRepository extends JpaRepository<VetRating, UUID> {
    Page<VetRating> findByVetProfile(VetProfile vetProfile, Pageable pageable);
    Optional<VetRating> findByService_ServiceId(UUID serviceId);
    boolean existsByService_ServiceId(UUID serviceId);

    @Query("SELECT AVG(r.rating) FROM VetRating r WHERE r.vetProfile.vetId = :vetId")
    Double getAverageRatingByVetId(@Param("vetId") UUID vetId);
}
