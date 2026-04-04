package tn.beecoders.elderly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "First name is mandatory") String firstName,
    @NotBlank(message = "Last name is mandatory") String lastName,
    @Email(message = "Email should be valid") @NotBlank(message = "Email is mandatory") String email,
    @NotBlank(message = "Password is mandatory") String password,
    @NotBlank(message = "Role is mandatory") String role
) {}
