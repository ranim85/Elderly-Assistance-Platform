package tn.beecoders.elderly.dto;

/** Partial update: only non-null fields are applied. */
public record AlertUpdateRequest(
        String alertType,
        String description,
        Boolean isResolved,
        String priority
) {}
