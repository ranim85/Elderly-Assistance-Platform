package tn.beecoders.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record MedicationCreateRequest(
    @NotNull Long elderlyPersonId,
    @NotBlank String name,
    String dosage,
    @NotNull LocalDateTime scheduledTime
) {}
