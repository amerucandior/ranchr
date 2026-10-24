package com.ranchr.ranchr.vet.dto;

import java.util.UUID;

public class VetEvents {
    public record ServiceCompletedEvent(UUID serviceId, String semenStrawBatch) {}
}
