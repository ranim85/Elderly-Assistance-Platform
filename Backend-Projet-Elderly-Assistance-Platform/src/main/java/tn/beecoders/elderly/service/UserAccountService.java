package tn.beecoders.elderly.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.beecoders.elderly.domain.ElderlyPerson;
import tn.beecoders.elderly.domain.Role;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.dto.RegisterRequest;
import tn.beecoders.elderly.dto.UserUpdateRequest;
import tn.beecoders.elderly.exception.BadRequestException;
import tn.beecoders.elderly.exception.ResourceNotFoundException;
import tn.beecoders.elderly.repository.ElderlyPersonRepository;
import tn.beecoders.elderly.repository.UserRepository;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

/**
 * Creates and updates {@link User} accounts; for {@link Role#ELDERLY} creates a matching {@link ElderlyPerson}.
 */
@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;
    private final ElderlyPersonRepository elderlyPersonRepository;
    private final PasswordEncoder passwordEncoder;
    private final CaregiverAssignmentService caregiverAssignmentService;

    @Transactional
    public User createUserFromRegister(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BadRequestException("User with this email already exists");
        }

        Role role = parseRole(request.role());
        validateFamilyAndCaregiverLinks(role, request.linkedElderlyPersonId(), request.caregiverId());

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .linkedElderlyPerson(resolveLinkedElderly(role, request.linkedElderlyPersonId()))
                .build();

        userRepository.save(user);

        if (role == Role.ELDERLY) {
            provisionElderlyPersonFromAccount(
                    request.firstName(), request.lastName(), resolveCaregiverForNewElderly(request.caregiverId()));
        }

        return user;
    }

    @Transactional
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(request.email())
                && userRepository.findByEmail(request.email()).isPresent()) {
            throw new BadRequestException("User with this email already exists");
        }

        Role role = parseRole(request.role());
        validateFamilyAndCaregiverLinks(role, request.linkedElderlyPersonId(), request.caregiverId());

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setRole(role);
        user.setLinkedElderlyPerson(resolveLinkedElderly(role, request.linkedElderlyPersonId()));

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        userRepository.save(user);

        if (role == Role.ELDERLY && request.caregiverId() != null) {
            syncElderlyCaregiverForAccount(user, request.caregiverId());
        }

        return user;
    }

    private void validateFamilyAndCaregiverLinks(Role role, Long linkedElderlyPersonId, Long caregiverId) {
        if (role == Role.FAMILY_MEMBER) {
            if (linkedElderlyPersonId == null) {
                throw new BadRequestException("Family members must be linked to an elderly person (linkedElderlyPersonId).");
            }
        } else if (linkedElderlyPersonId != null) {
            throw new BadRequestException("linkedElderlyPersonId is only allowed for role FAMILY_MEMBER.");
        }

        if (caregiverId != null && role != Role.ELDERLY) {
            throw new BadRequestException("caregiverId is only allowed for role ELDERLY.");
        }
    }

    private ElderlyPerson resolveLinkedElderly(Role role, Long linkedElderlyPersonId) {
        if (role != Role.FAMILY_MEMBER) {
            return null;
        }
        return elderlyPersonRepository.findById(linkedElderlyPersonId)
                .orElseThrow(() -> new BadRequestException("Elderly person not found for linkedElderlyPersonId."));
    }

    private User resolveCaregiverForNewElderly(Long caregiverId) {
        if (caregiverId == null) {
            return null;
        }
        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new BadRequestException("Caregiver user not found."));
        caregiverAssignmentService.validateCapacityForNewElderly(caregiver);
        return caregiver;
    }

    private void syncElderlyCaregiverForAccount(User elderlyUser, Long caregiverId) {
        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new BadRequestException("Caregiver user not found."));

        ElderlyPerson match = elderlyPersonRepository
                .findFirstByFirstNameIgnoreCaseAndLastNameIgnoreCase(
                        elderlyUser.getFirstName(), elderlyUser.getLastName())
                .orElse(null);

        if (match == null) {
            return;
        }

        caregiverAssignmentService.assignCaregiver(match, caregiver);
        elderlyPersonRepository.save(match);
    }

    private void provisionElderlyPersonFromAccount(String firstName, String lastName, User caregiver) {
        ElderlyPerson elderly = ElderlyPerson.builder()
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(LocalDate.of(1950, 1, 1))
                .address("Not specified")
                .medicalConditions("Linked from user account - complete intake as needed")
                .caregiver(null)
                .build();
        if (caregiver != null) {
            caregiverAssignmentService.assignCaregiver(elderly, caregiver);
        }
        elderlyPersonRepository.save(elderly);
    }

    private static Role parseRole(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("Role is required");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Invalid role. Allowed values: ADMIN, CAREGIVER, ELDERLY, FAMILY_MEMBER");
        }
    }
}
