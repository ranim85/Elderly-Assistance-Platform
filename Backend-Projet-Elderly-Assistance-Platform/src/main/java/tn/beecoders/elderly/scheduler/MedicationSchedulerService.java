package tn.beecoders.elderly.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.beecoders.elderly.domain.Alert;
import tn.beecoders.elderly.domain.AlertPriority;
import tn.beecoders.elderly.domain.Medication;
import tn.beecoders.elderly.repository.AlertRepository;
import tn.beecoders.elderly.repository.MedicationRepository;
import tn.beecoders.elderly.dto.AlertDTO;
import tn.beecoders.elderly.dto.ElderlySummaryDTO;
import tn.beecoders.elderly.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicationSchedulerService {

    private final MedicationRepository medicationRepository;
    private final AlertRepository alertRepository;
    private final NotificationService notificationService;

    // Run every 30 minutes
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void checkOverdueMedications() {
        log.info("Scanning for overdue medications...");
        // Add a 30 minute grace period before considering it fully missed
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        List<Medication> overdue = medicationRepository.findOverdueMedications(cutoff);

        for (Medication med : overdue) {
            String desc = "Missed Medication: " + med.getName() + " (" + med.getDosage() + ") scheduled at " + med.getScheduledTime();
            
            boolean exists = alertRepository.existsByElderlyPersonIdAndDescriptionAndIsResolvedFalse(
                    med.getElderlyPerson().getId(), desc);

            if (!exists) {
                Alert alert = Alert.builder()
                        .elderlyPerson(med.getElderlyPerson())
                        .alertType(Alert.AlertType.MEDICAL_EMERGENCY)
                        .priority(AlertPriority.HIGH)
                        .description(desc)
                        .timestamp(LocalDateTime.now())
                        .isResolved(false)
                        .build();
                
                Alert saved = alertRepository.save(alert);
                // Notification payload
                ElderlySummaryDTO summary = ElderlySummaryDTO.builder()
                        .id(saved.getElderlyPerson().getId())
                        .firstName(saved.getElderlyPerson().getFirstName())
                        .lastName(saved.getElderlyPerson().getLastName())
                        .build();

                AlertDTO dto = AlertDTO.builder()
                        .id(saved.getId())
                        .alertType(saved.getAlertType().name())
                        .priority(saved.getPriority() != null ? saved.getPriority().name() : AlertPriority.MEDIUM.name())
                        .description(saved.getDescription())
                        .timestamp(saved.getTimestamp())
                        .isResolved(saved.isResolved())
                        .elderlyPerson(summary)
                        .build();

                String caregiverEmail = saved.getElderlyPerson().getCaregiver() != null ? 
                        saved.getElderlyPerson().getCaregiver().getEmail() : null;
                notificationService.sendAlertToCaregiver(caregiverEmail, dto);
                
                log.warn("Generated missed medication alert for elderly ID {}", saved.getElderlyPerson().getId());
            }
        }
    }
}
