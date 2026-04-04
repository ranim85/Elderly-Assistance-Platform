package tn.beecoders.elderly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
    @Email(message = "Email should be valid") @NotBlank(message = "Email is mandatory") String email,
    @NotBlank(message = "Password is mandatory") String password
) {}
