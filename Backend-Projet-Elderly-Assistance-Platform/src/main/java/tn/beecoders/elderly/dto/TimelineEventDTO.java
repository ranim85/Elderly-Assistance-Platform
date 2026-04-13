package tn.beecoders.elderly.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record TimelineEventDTO(
    String id,
    LocalDateTime date,
    String eventType,
    String title,
    String description,
    String severity
) {}
