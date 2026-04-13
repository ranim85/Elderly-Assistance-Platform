package tn.beecoders.elderly.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AlertPageResponse(
        List<AlertDTO> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {}
