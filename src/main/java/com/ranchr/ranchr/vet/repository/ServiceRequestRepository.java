package com.ranchr.ranchr.vet.repository;

import com.ranchr.ranchr.vet.model.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, UUID> {
}
