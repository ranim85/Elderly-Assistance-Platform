package tn.beecoders.elderly.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record MedicationDTO(
    Long id,
    String name,
    String dosage,
    LocalDateTime scheduledTime,
    boolean isTaken,
    LocalDateTime timeTaken,
    Long elderlyPersonId
) {}
