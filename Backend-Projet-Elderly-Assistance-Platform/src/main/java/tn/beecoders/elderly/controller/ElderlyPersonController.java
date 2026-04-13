package tn.beecoders.elderly.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.beecoders.elderly.domain.Alert;
import tn.beecoders.elderly.domain.CareStatus;
import tn.beecoders.elderly.domain.ElderlyPerson;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.dto.AlertDTO;
import tn.beecoders.elderly.dto.AlertPageResponse;
import tn.beecoders.elderly.dto.CareStatusPatchRequest;
import tn.beecoders.elderly.dto.CaregiverAssignRequest;
import tn.beecoders.elderly.dto.CaregiverSummaryDTO;
import tn.beecoders.elderly.dto.ElderlyPersonDTO;
import tn.beecoders.elderly.exception.BadRequestException;
import tn.beecoders.elderly.exception.ResourceNotFoundException;
import tn.beecoders.elderly.repository.AlertRepository;
import tn.beecoders.elderly.repository.ElderlyPersonRepository;
import tn.beecoders.elderly.repository.UserRepository;
import tn.beecoders.elderly.service.AlertPermissionService;
import tn.beecoders.elderly.service.AlertSpecifications;
import tn.beecoders.elderly.service.CaregiverAssignmentService;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/elderly-persons")
@RequiredArgsConstructor
public class ElderlyPersonController {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_FAMILY_MEMBER = "ROLE_FAMILY_MEMBER";
    private static final String ROLE_ELDERLY = "ROLE_ELDERLY";

    private final ElderlyPersonRepository elderlyPersonRepository;
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final CaregiverAssignmentService caregiverAssignmentService;
    private final AlertPermissionService alertPermissionService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<ElderlyPersonDTO>> getAllElderlyPersons(Authentication authentication) {
        String email = authentication.getName();
        boolean isAdmin = hasAuthority(authentication, ROLE_ADMIN);

        List<ElderlyPerson> elderlyList;
        if (isAdmin) {
            elderlyList = elderlyPersonRepository.findAll();
        } else if (hasAuthority(authentication, ROLE_FAMILY_MEMBER)) {
            User u = userRepository.findByEmail(email).orElseThrow();
            if (u.getLinkedElderlyPerson() == null) {
                elderlyList = List.of();
            } else {
                elderlyList = List.of(u.getLinkedElderlyPerson());
            }
        } else if (hasAuthority(authentication, ROLE_ELDERLY)) {
            User u = userRepository.findByEmail(email).orElseThrow();
            elderlyList = elderlyPersonRepository
                    .findFirstByFirstNameIgnoreCaseAndLastNameIgnoreCase(u.getFirstName(), u.getLastName())
                    .map(List::of)
                    .orElse(List.of());
        } else {
            elderlyList = elderlyPersonRepository.findAllByCaregiver_Email(email);
        }

        List<ElderlyPersonDTO> dtos = elderlyList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/alerts")
    @Transactional(readOnly = true)
    public ResponseEntity<AlertPageResponse> alertsForElderly(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!canViewElderly(id, authentication)) {
            throw new AccessDeniedException("You cannot view alerts for this elderly person");
        }
        Specification<Alert> spec = Specification.where(alertPermissionService.alertScope(authentication))
                .and(AlertSpecifications.elderlyIdEqual(id));
        var result = alertRepository.findAll(
                spec,
                PageRequest.of(page, Math.min(size, 200), Sort.by(Sort.Direction.DESC, "timestamp")));
        List<AlertDTO> content = result.getContent().stream()
                .map(this::mapAlertToDto)
                .toList();
        return ResponseEntity.ok(AlertPageResponse.builder()
                .content(content)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .number(result.getNumber())
                .size(result.getSize())
                .build());
    }

    @PutMapping("/{id}/caregiver")
    @Transactional
    public ResponseEntity<ElderlyPersonDTO> assignCaregiver(
            @PathVariable Long id,
            @Valid @RequestBody CaregiverAssignRequest body) {
        ElderlyPerson elderly = elderlyPersonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Elderly person not found"));
        User caregiver = userRepository.findById(body.caregiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Caregiver user not found"));
        caregiverAssignmentService.assignCaregiver(elderly, caregiver);
        ElderlyPerson saved = elderlyPersonRepository.save(elderly);
        return ResponseEntity.ok(mapToDTO(saved));
    }

    @PutMapping("/{id}/care-status")
    @Transactional
    public ResponseEntity<ElderlyPersonDTO> updateCareStatus(
            @PathVariable Long id,
            @Valid @RequestBody CareStatusPatchRequest body,
            Authentication authentication) {
        ElderlyPerson elderly = elderlyPersonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Elderly person not found"));
        if (!canUpdateCareStatus(elderly, authentication)) {
            throw new AccessDeniedException("Only an administrator or assigned caregiver can update care status");
        }
        elderly.setCareStatus(parseCareStatus(body.careStatus()));
        return ResponseEntity.ok(mapToDTO(elderlyPersonRepository.save(elderly)));
    }

    private boolean canUpdateCareStatus(ElderlyPerson elderly, Authentication authentication) {
        if (hasAuthority(authentication, ROLE_ADMIN)) {
            return true;
        }
        User cg = elderly.getCaregiver();
        return cg != null && cg.getEmail().equalsIgnoreCase(authentication.getName());
    }

    private boolean canViewElderly(Long elderlyId, Authentication authentication) {
        ElderlyPerson e = elderlyPersonRepository.findById(elderlyId).orElse(null);
        if (e == null) {
            return false;
        }
        if (hasAuthority(authentication, ROLE_ADMIN)) {
            return true;
        }
        User u = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (u == null) {
            return false;
        }
        if (hasAuthority(authentication, ROLE_FAMILY_MEMBER)
                && u.getLinkedElderlyPerson() != null
                && u.getLinkedElderlyPerson().getId().equals(elderlyId)) {
            return true;
        }
        if (hasAuthority(authentication, ROLE_ELDERLY)
                && e.getFirstName().equalsIgnoreCase(u.getFirstName())
                && e.getLastName().equalsIgnoreCase(u.getLastName())) {
            return true;
        }
        return e.getCaregiver() != null && e.getCaregiver().getEmail().equalsIgnoreCase(authentication.getName());
    }

    private static boolean hasAuthority(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private static CareStatus parseCareStatus(String raw) {
        try {
            return CareStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid careStatus. Allowed: STABLE, WARNING, CRITICAL");
        }
    }

    private ElderlyPersonDTO mapToDTO(ElderlyPerson person) {
        CaregiverSummaryDTO caregiverDto = null;
        User caregiver = person.getCaregiver();
        if (caregiver != null) {
            caregiverDto = CaregiverSummaryDTO.builder()
                    .id(caregiver.getId())
                    .firstName(caregiver.getFirstName())
                    .lastName(caregiver.getLastName())
                    .build();
        }

        CareStatus cs = person.getCareStatus() != null ? person.getCareStatus() : CareStatus.STABLE;
        return ElderlyPersonDTO.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .dateOfBirth(person.getDateOfBirth())
                .address(person.getAddress())
                .medicalConditions(person.getMedicalConditions())
                .careStatus(cs.name())
                .caregiver(caregiverDto)
                .build();
    }

    private AlertDTO mapAlertToDto(Alert alert) {
        var elderly = alert.getElderlyPerson();
        var elderlySummary = tn.beecoders.elderly.dto.ElderlySummaryDTO.builder()
                .id(elderly.getId())
                .firstName(elderly.getFirstName())
                .lastName(elderly.getLastName())
                .build();
        User rb = alert.getResolvedBy();
        String priority = alert.getPriority() != null ? alert.getPriority().name() : "MEDIUM";
        return AlertDTO.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType().name())
                .priority(priority)
                .description(alert.getDescription())
                .timestamp(alert.getTimestamp())
                .isResolved(alert.isResolved())
                .resolvedAt(alert.getResolvedAt())
                .resolvedByEmail(rb != null ? rb.getEmail() : null)
                .elderlyPerson(elderlySummary)
                .build();
    }
}
