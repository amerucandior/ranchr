package com.ranchr.ranchr.vet.repository;

import com.ranchr.ranchr.vet.model.VetProfile;
import com.ranchr.ranchr.vet.model.enums.Available;
import com.ranchr.ranchr.vet.model.enums.ServicesOffered;
import com.ranchr.ranchr.vet.model.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface VetProfileRepository extends JpaRepository<VetProfile, UUID> {
    Optional<VetProfile> findByVetId(UUID vetId);
    List<VetProfile> findAllByAvailableAndVerified(Available available, VerificationStatus verified);
    List<VetProfile> findAllByVerified(VerificationStatus verified);
    Set<VetProfile> findAllByServicesOffered(Set<ServicesOffered> servicesOffered);
    Boolean existsByVetId(UUID vetId);
}
