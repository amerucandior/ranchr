package com.ranchr.ranchr.reproduction.repository;

import com.ranchr.authentication.model.User;
import com.ranchr.ranchr.reproduction.model.ReproductiveCycle;
import com.ranchr.ranchr.reproduction.model.enums.ReproductiveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReproductiveCycleRepository extends JpaRepository<ReproductiveCycle, UUID> {
    Page<ReproductiveCycle> findByFarmer(User farmer, Pageable pageable);
    List<ReproductiveCycle> findByFarmerAndStatus(User farmer, ReproductiveStatus status);
    Optional<ReproductiveCycle> findTopByFarmerAndStatusOrderByCreatedAtDesc(User farmer, ReproductiveStatus status);
    Optional<ReproductiveCycle> findByServiceRequest_ServiceId(UUID serviceId);

    // Queries for the automated scheduler
    List<ReproductiveCycle> findByStatusAndNonReturnCheckDateLessThanEqualAndDay21AlertSentFalse(
            ReproductiveStatus status, LocalDate date);

    List<ReproductiveCycle> findByStatusAndPregnancyDiagnosisDateLessThanEqualAndDay60AlertSentFalse(
            ReproductiveStatus status, LocalDate date);

    List<ReproductiveCycle> findByStatusAndExpectedCalvingDateLessThanEqualAndCalvingAlertSentFalse(
            ReproductiveStatus status, LocalDate date);
}
