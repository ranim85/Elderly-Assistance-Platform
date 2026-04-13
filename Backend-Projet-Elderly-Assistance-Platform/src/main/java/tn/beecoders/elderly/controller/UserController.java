package tn.beecoders.elderly.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.beecoders.elderly.domain.Role;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.exception.BadRequestException;
import tn.beecoders.elderly.repository.UserRepository;
import tn.beecoders.elderly.dto.UserDTO;
import tn.beecoders.elderly.dto.RegisterRequest;
import tn.beecoders.elderly.dto.UserUpdateRequest;
import tn.beecoders.elderly.service.UserAccountService;

import java.util.stream.Collectors;

/**
 * User administration and profile.
 * <p>List ({@code GET /api/users}) and delete are enforced as {@code ADMIN} in {@link tn.beecoders.elderly.config.SecurityConfig};
 * {@code GET /api/users/me} is available to any authenticated principal.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserAccountService userAccountService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<java.util.List<UserDTO>> getAllUsers() {
        var users = userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody RegisterRequest request) {
        User user = userAccountService.createUserFromRegister(request);
        return ResponseEntity.ok(mapToDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        User user = userAccountService.updateUser(id, request);
        return ResponseEntity.ok(mapToDTO(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) <= 1) {
            throw new BadRequestException("Cannot delete the last administrator account.");
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private UserDTO mapToDTO(User user) {
        Long linkedId = user.getLinkedElderlyPerson() == null ? null : user.getLinkedElderlyPerson().getId();
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .linkedElderlyPersonId(linkedId)
                .build();
    }
}
