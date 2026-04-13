package tn.beecoders.elderly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.beecoders.elderly.domain.*;
import tn.beecoders.elderly.dto.*;
import tn.beecoders.elderly.exception.ResourceNotFoundException;
import tn.beecoders.elderly.repository.AlertRepository;
import tn.beecoders.elderly.repository.ElderlyPersonRepository;
import tn.beecoders.elderly.repository.ElderlySettingsRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final ElderlySettingsRepository settingsRepository;
    private final ElderlyPersonRepository elderlyPersonRepository;
    private final AlertRepository alertRepository;
    private final NotificationService notificationService;

    private static final double EARTH_RADIUS_METERS = 6371000;

    @Transactional
    public void processLocationPing(LocationPingDTO ping) {
        settingsRepository.findByElderlyPersonId(ping.getElderlyId()).ifPresent(settings -> {
            if (settings.getHomeLatitude() != null && settings.getHomeLongitude() != null && settings.getSafeZoneRadius() != null) {
                
                double dist = calculateHaversineDistance(
                        settings.getHomeLatitude(), settings.getHomeLongitude(),
                        ping.getLatitude(), ping.getLongitude());

                log.info("Elderly {} is {} meters away from home", ping.getElderlyId(), Math.round(dist));

                if (dist > settings.getSafeZoneRadius()) {
                    triggerWanderingAlert(settings.getElderlyPerson(), dist);
                }
            }
        });
    }

    private void triggerWanderingAlert(ElderlyPerson elderly, double distance) {
        String description = "WANDERING EMERGENCY: Patient breached safe zone boundary. Distance: " + Math.round(distance) + "m";
        
        boolean exists = alertRepository.existsByElderlyPersonIdAndDescriptionAndIsResolvedFalse(elderly.getId(), description);
        if (!exists) {
            Alert alert = Alert.builder()
                    .elderlyPerson(elderly)
                    .alertType(Alert.AlertType.WANDERING_EMERGENCY)
                    .priority(AlertPriority.URGENT)
                    .description(description)
                    .timestamp(LocalDateTime.now())
                    .isResolved(false)
                    .build();
            
            Alert saved = alertRepository.save(alert);
            
            ElderlySummaryDTO summary = ElderlySummaryDTO.builder()
                    .id(saved.getElderlyPerson().getId())
                    .firstName(saved.getElderlyPerson().getFirstName())
                    .lastName(saved.getElderlyPerson().getLastName())
                    .build();

            AlertDTO dto = AlertDTO.builder()
                    .id(saved.getId())
                    .alertType(saved.getAlertType().name())
                    .priority(saved.getPriority().name())
                    .description(saved.getDescription())
                    .timestamp(saved.getTimestamp())
                    .isResolved(saved.isResolved())
                    .elderlyPerson(summary)
                    .build();

            String email = saved.getElderlyPerson().getCaregiver() != null ? saved.getElderlyPerson().getCaregiver().getEmail() : null;
            notificationService.sendAlertToCaregiver(email, dto);
            
            log.warn("Generated Wandering Alert for Elderly ID {}", elderly.getId());
        }
    }

    @Transactional(readOnly = true)
    public ElderlySettingsDTO getSettings(Long elderlyId) {
        return settingsRepository.findByElderlyPersonId(elderlyId)
                .map(this::mapToDTO)
                .orElseGet(() -> ElderlySettingsDTO.builder().elderlyId(elderlyId).build());
    }

    @Transactional
    public ElderlySettingsDTO updateSettings(ElderlySettingsDTO dto) {
        ElderlySettings settings = settingsRepository.findByElderlyPersonId(dto.getElderlyId())
                .orElseGet(() -> {
                    ElderlyPerson ep = elderlyPersonRepository.findById(dto.getElderlyId())
                            .orElseThrow(() -> new ResourceNotFoundException("Elderly not found"));
                    return ElderlySettings.builder().elderlyPerson(ep).build();
                });

        settings.setHomeLatitude(dto.getHomeLatitude());
        settings.setHomeLongitude(dto.getHomeLongitude());
        settings.setSafeZoneRadius(dto.getSafeZoneRadius());

        return mapToDTO(settingsRepository.save(settings));
    }

    private ElderlySettingsDTO mapToDTO(ElderlySettings settings) {
        return ElderlySettingsDTO.builder()
                .id(settings.getId())
                .elderlyId(settings.getElderlyPerson().getId())
                .homeLatitude(settings.getHomeLatitude())
                .homeLongitude(settings.getHomeLongitude())
                .safeZoneRadius(settings.getSafeZoneRadius())
                .build();
    }

    public double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
