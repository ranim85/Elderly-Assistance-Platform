package tn.beecoders.elderly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Admin update of a user. Blank {@code password} means leave unchanged.
 */
public record UserUpdateRequest(
        @NotBlank(message = "First name is mandatory") String firstName,
        @NotBlank(message = "Last name is mandatory") String lastName,
        @Email(message = "Email should be valid") @NotBlank(message = "Email is mandatory") String email,
        String password,
        @NotBlank(message = "Role is mandatory") String role,
        Long linkedElderlyPersonId,
        Long caregiverId
) {}
