package tn.beecoders.elderly.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.dto.TimelineEventDTO;
import tn.beecoders.elderly.exception.ResourceNotFoundException;
import tn.beecoders.elderly.repository.AlertRepository;
import tn.beecoders.elderly.repository.AppointmentRepository;
import tn.beecoders.elderly.repository.HealthRecordRepository;
import tn.beecoders.elderly.repository.MedicationRepository;
import tn.beecoders.elderly.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
public class FamilyTimelineController {

    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final AppointmentRepository appointmentRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final MedicationRepository medicationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<TimelineEventDTO>> getFamilyTimeline(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getLinkedElderlyPerson() == null) {
            return ResponseEntity.ok(List.of()); // Not linked yet
        }

        Long elderlyId = user.getLinkedElderlyPerson().getId();
        List<TimelineEventDTO> timeline = new ArrayList<>();

        alertRepository.findByElderlyPersonId(elderlyId).forEach(a -> timeline.add(TimelineEventDTO.builder()
                .id("AL-" + a.getId())
                .date(a.getTimestamp())
                .eventType("ALERT")
                .title(a.getAlertType().name())
                .description(a.getDescription())
                .severity("CRITICAL")
                .build()));

        appointmentRepository.findByElderlyPersonId(elderlyId).forEach(ap -> timeline.add(TimelineEventDTO.builder()
                .id("AP-" + ap.getId())
                .date(ap.getAppointmentDate())
                .eventType("APPOINTMENT")
                .title("Medical Appointment")
                .description("Doctor: " + ap.getDoctorName() + " - " + ap.getPurpose())
                .severity("NORMAL")
                .build()));

        healthRecordRepository.findByElderlyPersonId(elderlyId).forEach(h -> timeline.add(TimelineEventDTO.builder()
                .id("HR-" + h.getId())
                .date(h.getRecordedAt())
                .eventType("HEALTH_RECORD")
                .title("Vitals Checked")
                .description("BP: " + h.getBloodPressure() + ", HR: " + h.getHeartRate() + ", Sugar: " + h.getBloodSugar())
                .severity("INFO")
                .build()));

        medicationRepository.findByElderlyPersonId(elderlyId).forEach(m -> timeline.add(TimelineEventDTO.builder()
                .id("MD-" + m.getId())
                .date(m.isTaken() ? m.getTimeTaken() : m.getScheduledTime())
                .eventType("MEDICATION")
                .title("Medication: " + m.getName())
                .description("Dosage: " + m.getDosage() + " - " + (m.isTaken() ? "Taken" : "Scheduled"))
                .severity(m.isTaken() ? "SUCCESS" : "WARNING")
                .build()));

        timeline.sort(Comparator.comparing(TimelineEventDTO::date).reversed());

        return ResponseEntity.ok(timeline);
    }
}
