package tn.beecoders.elderly.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
    String token,
    String refreshToken,
    String type,
    String email,
    String role
) {}
