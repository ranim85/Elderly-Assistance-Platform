package tn.beecoders.elderly.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.beecoders.elderly.domain.CareStatus;
import tn.beecoders.elderly.domain.Role;
import tn.beecoders.elderly.dto.DashboardStatsResponse;
import tn.beecoders.elderly.repository.AlertRepository;
import tn.beecoders.elderly.repository.ElderlyPersonRepository;
import tn.beecoders.elderly.repository.UserRepository;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ElderlyPersonRepository elderlyPersonRepository;
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;

    @GetMapping("/stats")
    @Transactional(readOnly = true)
    public ResponseEntity<DashboardStatsResponse> getStats(Authentication authentication) {
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        long totalAssisted;
        long activeCaregivers;
        long urgentAlerts;
        long elderlyStable;
        long elderlyWarning;
        long elderlyCritical;

        if (isAdmin) {
            totalAssisted = elderlyPersonRepository.count();
            activeCaregivers = userRepository.countByRole(Role.CAREGIVER);
            urgentAlerts = alertRepository.countByIsResolvedFalse();
            elderlyStable = elderlyPersonRepository.countByCareStatus(CareStatus.STABLE);
            elderlyWarning = elderlyPersonRepository.countByCareStatus(CareStatus.WARNING);
            elderlyCritical = elderlyPersonRepository.countByCareStatus(CareStatus.CRITICAL);
        } else {
            totalAssisted = elderlyPersonRepository.countByCaregiver_Email(email);
            activeCaregivers = 1;
            urgentAlerts = alertRepository.countByIsResolvedFalseAndElderlyPerson_Caregiver_Email(email);
            elderlyStable = elderlyPersonRepository.countByCaregiver_EmailAndCareStatus(email, CareStatus.STABLE);
            elderlyWarning = elderlyPersonRepository.countByCaregiver_EmailAndCareStatus(email, CareStatus.WARNING);
            elderlyCritical = elderlyPersonRepository.countByCaregiver_EmailAndCareStatus(email, CareStatus.CRITICAL);
        }

        return ResponseEntity.ok(DashboardStatsResponse.builder()
                .totalAssisted(totalAssisted)
                .activeCaregivers(activeCaregivers)
                .urgentAlerts(urgentAlerts)
                .elderlyStable(elderlyStable)
                .elderlyWarning(elderlyWarning)
                .elderlyCritical(elderlyCritical)
                .build());
    }
}
