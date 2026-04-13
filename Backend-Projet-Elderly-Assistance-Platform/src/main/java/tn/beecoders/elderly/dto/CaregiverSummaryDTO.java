package tn.beecoders.elderly.dto;

import lombok.Builder;

/**
 * Minimal caregiver projection for nested API responses (e.g. inside {@link ElderlyPersonDTO}).
 */
@Builder
public record CaregiverSummaryDTO(
        Long id,
        String firstName,
        String lastName
) {}
