package tn.beecoders.elderly.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.beecoders.elderly.domain.Alert;
import tn.beecoders.elderly.domain.AlertPriority;
import tn.beecoders.elderly.domain.ElderlyPerson;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.dto.AlertCreateRequest;
import tn.beecoders.elderly.dto.AlertDTO;
import tn.beecoders.elderly.dto.AlertPageResponse;
import tn.beecoders.elderly.dto.AlertUpdateRequest;
import tn.beecoders.elderly.dto.ElderlySummaryDTO;
import tn.beecoders.elderly.exception.BadRequestException;
import tn.beecoders.elderly.exception.ResourceNotFoundException;
import tn.beecoders.elderly.repository.AlertRepository;
import tn.beecoders.elderly.repository.ElderlyPersonRepository;
import tn.beecoders.elderly.repository.UserRepository;
import tn.beecoders.elderly.service.AlertPermissionService;
import tn.beecoders.elderly.service.AlertSpecifications;
import tn.beecoders.elderly.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;
    private final ElderlyPersonRepository elderlyPersonRepository;
    private final UserRepository userRepository;
    private final AlertPermissionService alertPermissionService;
    private final NotificationService notificationService;

    @GetMapping("/recent")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AlertDTO>> getRecentAlerts(Authentication authentication) {
        Specification<Alert> spec = Specification.where(alertPermissionService.alertScope(authentication));
        Page<Alert> page = alertRepository.findAll(
                spec,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "timestamp")));
        return ResponseEntity.ok(page.getContent().stream().map(this::mapToDTO).collect(Collectors.toList()));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<AlertPageResponse> getAllAlerts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        Specification<Alert> spec = Specification.where(alertPermissionService.alertScope(authentication))
                .and(AlertSpecifications.isResolvedEqual(resolved));
        if (priority != null && !priority.isBlank()) {
            try {
                spec = spec.and(AlertSpecifications.priorityEqual(priority));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid priority filter. Use LOW, MEDIUM, HIGH, or URGENT.");
            }
        }
        spec = spec.and(AlertSpecifications.timestampFrom(from)).and(AlertSpecifications.timestampTo(to));

        Page<Alert> result = alertRepository.findAll(
                spec,
                PageRequest.of(page, Math.min(size, 200), Sort.by(Sort.Direction.DESC, "timestamp")));

        return ResponseEntity.ok(AlertPageResponse.builder()
                .content(result.getContent().stream().map(this::mapToDTO).toList())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .number(result.getNumber())
                .size(result.getSize())
                .build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<AlertDTO> createAlert(
            @Valid @RequestBody AlertCreateRequest request,
            Authentication authentication) {
        ElderlyPerson elderly = elderlyPersonRepository.findById(request.elderlyPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Elderly person not found"));
        alertPermissionService.assertCanCreateAlert(authentication, elderly);

        Alert alert = Alert.builder()
                .elderlyPerson(elderly)
                .alertType(parseAlertType(request.alertType()))
                .priority(parsePriorityForCreate(request.priority()))
                .description(request.description())
                .timestamp(LocalDateTime.now())
                .isResolved(false)
                .resolvedAt(null)
                .resolvedBy(null)
                .build();

        Alert saved = alertRepository.save(alert);
        AlertDTO dto = mapToDTO(saved);

        String caregiverEmail = elderly.getCaregiver() != null ? elderly.getCaregiver().getEmail() : null;
        notificationService.sendAlertToCaregiver(caregiverEmail, dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<AlertDTO> updateAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertUpdateRequest request,
            Authentication authentication) {
        return alertRepository.findById(id).map(alert -> {
            alertPermissionService.assertCanModifyAlert(authentication, alert);
            if (request.alertType() != null && !request.alertType().isBlank()) {
                alert.setAlertType(parseAlertType(request.alertType()));
            }
            if (request.description() != null) {
                alert.setDescription(request.description());
            }
            if (request.isResolved() != null) {
                applyResolvedState(alert, request.isResolved(), authentication);
            }
            if (request.priority() != null) {
                if (request.priority().isBlank()) {
                    throw new BadRequestException("priority cannot be blank; omit the field to leave unchanged.");
                }
                alert.setPriority(parsePriority(request.priority()));
            }
            return ResponseEntity.ok(mapToDTO(alertRepository.save(alert)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/resolve")
    @Transactional
    public ResponseEntity<AlertDTO> resolveAlert(
            @PathVariable Long id,
            Authentication authentication) {
        return alertRepository.findById(id).map(alert -> {
            alertPermissionService.assertCanResolve(authentication, alert);
            applyResolvedState(alert, true, authentication);
            var saved = alertRepository.save(alert);
            return ResponseEntity.ok(mapToDTO(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    private void applyResolvedState(Alert alert, boolean resolved, Authentication authentication) {
        alert.setResolved(resolved);
        if (resolved) {
            alert.setResolvedAt(LocalDateTime.now());
            alert.setResolvedBy(userRepository.findByEmail(authentication.getName()).orElse(null));
        } else {
            alert.setResolvedAt(null);
            alert.setResolvedBy(null);
        }
    }

    private static Alert.AlertType parseAlertType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("alertType is required");
        }
        try {
            return Alert.AlertType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid alertType. Allowed: SOS, MEDICAL_EMERGENCY, FALL_DETECTED, WANDERING_EMERGENCY");
        }
    }

    private static AlertPriority parsePriorityForCreate(String raw) {
        if (raw == null || raw.isBlank()) {
            return AlertPriority.MEDIUM;
        }
        return parsePriority(raw);
    }

    private static AlertPriority parsePriority(String raw) {
        try {
            return AlertPriority.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid priority. Allowed: LOW, MEDIUM, HIGH, URGENT");
        }
    }

    private static String effectivePriority(Alert alert) {
        return alert.getPriority() != null ? alert.getPriority().name() : AlertPriority.MEDIUM.name();
    }

    private AlertDTO mapToDTO(Alert alert) {
        var elderly = alert.getElderlyPerson();
        ElderlySummaryDTO elderlySummary = ElderlySummaryDTO.builder()
                .id(elderly.getId())
                .firstName(elderly.getFirstName())
                .lastName(elderly.getLastName())
                .build();

        User rb = alert.getResolvedBy();
        return AlertDTO.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType().name())
                .priority(effectivePriority(alert))
                .description(alert.getDescription())
                .timestamp(alert.getTimestamp())
                .isResolved(alert.isResolved())
                .resolvedAt(alert.getResolvedAt())
                .resolvedByEmail(rb != null ? rb.getEmail() : null)
                .elderlyPerson(elderlySummary)
                .build();
    }
}
