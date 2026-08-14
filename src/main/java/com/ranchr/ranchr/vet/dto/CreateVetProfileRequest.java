package com.ranchr.ranchr.vet.dto;

import com.ranchr.ranchr.vet.model.enums.ServicesOffered;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CreateVetProfileRequest(
        @NotNull
        @NotEmpty
        Set<ServicesOffered> servicesOffered,

        Integer experienceYears
) {
}