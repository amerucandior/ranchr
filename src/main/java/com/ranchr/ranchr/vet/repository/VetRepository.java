package com.ranchr.ranchr.vet.repository;

import com.ranchr.ranchr.vet.model.VetProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VetRepository extends JpaRepository<VetProfile, UUID> {
}
