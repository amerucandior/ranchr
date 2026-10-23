package com.ranchr.ranchr.vet.repository;

import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.vet.model.ServiceRequest;
import com.ranchr.ranchr.vet.model.VetProfile;
import com.ranchr.ranchr.vet.model.enums.ServiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
    Page<ServiceRequest> findByFarmer(User farmer, Pageable pageable);
    Page<ServiceRequest> findByVetProfile(VetProfile vetProfile, Pageable pageable);
    Page<ServiceRequest> findByVetProfileAndStatus(VetProfile vetProfile, ServiceStatus status, Pageable pageable);
    List<ServiceRequest> findByVetProfileAndStatus(VetProfile vetProfile, ServiceStatus status);
    Optional<ServiceRequest> findTopByVetProfileAndStatusOrderByCreatedAtDesc(VetProfile vetProfile, ServiceStatus status);
    Optional<ServiceRequest> findTopByFarmerAndStatusOrderByCreatedAtDesc(User farmer, ServiceStatus status);
    Optional<ServiceRequest> findTopByVetProfileIsNullAndStatusOrderByCreatedAtDesc(ServiceStatus status);
    Page<ServiceRequest> findByVetProfileIsNullAndStatus(ServiceStatus status, Pageable pageable);
    Page<ServiceRequest> findByVetProfileIsNullAndStatusAndLocationContainingIgnoreCase(
            ServiceStatus status, String location, Pageable pageable);
}
