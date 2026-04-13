package tn.beecoders.elderly.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.beecoders.elderly.domain.Alert;
import tn.beecoders.elderly.domain.Role;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.repository.AlertRepository;
import tn.beecoders.elderly.repository.ElderlyPersonRepository;
import tn.beecoders.elderly.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ElderlyPersonRepository elderlyPersonRepository;
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> buildSummary(
            Authentication authentication,
            LocalDate from,
            LocalDate to,
            Long caregiverId) {

        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        LocalDateTime fromDt = from != null ? from.atStartOfDay() : LocalDateTime.now().minusDays(30).with(LocalTime.MIN);
        LocalDateTime toDt = to != null ? to.atTime(LocalTime.MAX) : LocalDateTime.now();

        Specification<Alert> spec = Specification
                .where(AlertSpecifications.timestampFrom(fromDt))
                .and(AlertSpecifications.timestampTo(toDt));

        if (isAdmin) {
            if (caregiverId != null) {
                User cg = userRepository.findById(caregiverId).orElse(null);
                if (cg != null && cg.getRole() == Role.CAREGIVER) {
                    spec = spec.and((root, q, cb) -> cb.equal(
                            root.join("elderlyPerson").join("caregiver").get("id"), caregiverId));
                }
            }
        } else {
            spec = spec.and((root, q, cb) -> cb.equal(
                    root.join("elderlyPerson").join("caregiver").get("email"), email));
        }

        List<Alert> alertsInRange = alertRepository.findAll(spec);

        Map<String, Long> alertsByDay = alertsInRange.stream()
                .filter(a -> a.getTimestamp() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getTimestamp().toLocalDate().toString(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        long totalElderly;
        long totalCaregivers;
        long unresolvedAlerts;
        if (isAdmin) {
            totalElderly = elderlyPersonRepository.count();
            totalCaregivers = userRepository.countByRole(Role.CAREGIVER);
            unresolvedAlerts = alertRepository.countByIsResolvedFalse();
        } else {
            totalElderly = elderlyPersonRepository.countByCaregiver_Email(email);
            totalCaregivers = 1;
            unresolvedAlerts = alertRepository.countByIsResolvedFalseAndElderlyPerson_Caregiver_Email(email);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("status", "SUCCESS");
        report.put("message", "Report generated successfully.");
        report.put("totalElderly", totalElderly);
        report.put("totalCaregivers", totalCaregivers);
        report.put("unresolvedAlerts", unresolvedAlerts);
        report.put("alertsInRangeCount", alertsInRange.size());
        report.put("alertsByDay", alertsByDay);
        report.put("from", fromDt.toLocalDate().toString());
        report.put("to", toDt.toLocalDate().toString());
        return report;
    }

    @Transactional(readOnly = true)
    public String buildSummaryCsv(Authentication authentication, LocalDate from, LocalDate to, Long caregiverId) {
        Map<String, Object> data = buildSummary(authentication, from, to, caregiverId);
        @SuppressWarnings("unchecked")
        Map<String, Long> byDay = (Map<String, Long>) data.getOrDefault("alertsByDay", Map.of());
        StringBuilder sb = new StringBuilder();
        sb.append("metric,value\n");
        sb.append("totalElderly,").append(data.get("totalElderly")).append('\n');
        sb.append("totalCaregivers,").append(data.get("totalCaregivers")).append('\n');
        sb.append("unresolvedAlerts,").append(data.get("unresolvedAlerts")).append('\n');
        sb.append("alertsInRangeCount,").append(data.get("alertsInRangeCount")).append('\n');
        sb.append("day,alertCount\n");
        byDay.forEach((day, c) -> sb.append(day).append(',').append(c).append('\n'));
        return sb.toString();
    }
}
