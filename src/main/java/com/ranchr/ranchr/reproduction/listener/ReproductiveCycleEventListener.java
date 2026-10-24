package com.ranchr.ranchr.reproduction.listener;

import com.ranchr.ranchr.reproduction.service.ReproductiveCycleService;
import com.ranchr.ranchr.vet.dto.VetEvents;
import com.ranchr.ranchr.vet.repository.ServiceRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReproductiveCycleEventListener {

    private final ReproductiveCycleService reproductiveCycleService;
    private final ServiceRequestRepository serviceRequestRepository;

    @EventListener
    public void onServiceCompleted(VetEvents.ServiceCompletedEvent event) {
        serviceRequestRepository.findById(event.serviceId()).ifPresent(req -> {
            boolean hasStraw = event.semenStrawBatch() != null && !event.semenStrawBatch().isBlank();
            boolean mentionsBreeding = (req.getSymptoms() != null && (
                    req.getSymptoms().toLowerCase().contains("heat") ||
                    req.getSymptoms().toLowerCase().contains("inseminat") ||
                    req.getSymptoms().toLowerCase().contains("breed")
            ));

            if (hasStraw || mentionsBreeding) {
                log.info("[Reproduction Event] Initializing reproductive cycle for serviceId: {}", event.serviceId());
                reproductiveCycleService.createCycleFromServiceRequest(req, event.semenStrawBatch());
            }
        });
    }
}
