package tn.beecoders.elderly.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AlertDTO(
        Long id,
        String alertType,
        String priority,
        String description,
        LocalDateTime timestamp,
        boolean isResolved,
        LocalDateTime resolvedAt,
        String resolvedByEmail,
        ElderlySummaryDTO elderlyPerson
) {}
