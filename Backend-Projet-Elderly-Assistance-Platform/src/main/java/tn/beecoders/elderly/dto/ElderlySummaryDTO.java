package tn.beecoders.elderly.dto;

import lombok.Builder;

/**
 * Minimal elderly projection for nested API responses (e.g. inside {@link AlertDTO}).
 */
@Builder
public record ElderlySummaryDTO(
        Long id,
        String firstName,
        String lastName
) {}
