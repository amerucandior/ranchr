package com.ranchr.ranchr.vet.repository;

import com.ranchr.ranchr.vet.model.VetRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VetRatingRepository extends JpaRepository<VetRating, UUID> {
}
