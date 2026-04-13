package tn.beecoders.elderly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * @param linkedElderlyPersonId Required when {@code role} is {@code FAMILY_MEMBER}: elderly person this account supports.
 * @param caregiverId           Optional when {@code role} is {@code ELDERLY}: caregiver assigned to the created elderly profile (subject to capacity).
 */
public record RegisterRequest(
        @NotBlank(message = "First name is mandatory") String firstName,
        @NotBlank(message = "Last name is mandatory") String lastName,
        @Email(message = "Email should be valid") @NotBlank(message = "Email is mandatory") String email,
        @NotBlank(message = "Password is mandatory") String password,
        @NotBlank(message = "Role is mandatory") String role,
        Long linkedElderlyPersonId,
        Long caregiverId
) {}
