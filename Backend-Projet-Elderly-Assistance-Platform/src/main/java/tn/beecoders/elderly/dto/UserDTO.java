package tn.beecoders.elderly.dto;

import lombok.Builder;

@Builder
public record UserDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    String role
) {}
