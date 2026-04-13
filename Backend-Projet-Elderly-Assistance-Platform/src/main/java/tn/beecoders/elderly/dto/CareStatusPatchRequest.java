package tn.beecoders.elderly.dto;

import jakarta.validation.constraints.NotBlank;

public record CareStatusPatchRequest(@NotBlank String careStatus) {}
