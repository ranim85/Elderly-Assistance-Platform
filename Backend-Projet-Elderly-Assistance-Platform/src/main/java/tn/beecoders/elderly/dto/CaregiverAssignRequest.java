package tn.beecoders.elderly.dto;

import jakarta.validation.constraints.NotNull;

public record CaregiverAssignRequest(@NotNull Long caregiverId) {}
