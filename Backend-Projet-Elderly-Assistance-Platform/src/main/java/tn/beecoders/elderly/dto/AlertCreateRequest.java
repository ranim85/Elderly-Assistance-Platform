package tn.beecoders.elderly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlertCreateRequest(
        @NotNull Long elderlyPersonId,
        @NotBlank String alertType,
        @NotBlank String description,
        /** Optional; omitted or blank defaults to {@code MEDIUM}. */
        String priority
) {}
