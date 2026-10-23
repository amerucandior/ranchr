package com.ranchr.ranchr.vet.dto;

public record CompleteServiceRequestDto(
        String semenStrawBatchNumber,
        String diagnosisNotes,
        String prescribedMedication,
        String followUpInstructions
) {
}
