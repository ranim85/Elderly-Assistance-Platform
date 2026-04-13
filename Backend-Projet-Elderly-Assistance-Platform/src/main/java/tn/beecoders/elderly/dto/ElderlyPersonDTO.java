package tn.beecoders.elderly.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ElderlyPersonDTO(
        Long id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        String medicalConditions,
        String careStatus,
        CaregiverSummaryDTO caregiver
) {}
