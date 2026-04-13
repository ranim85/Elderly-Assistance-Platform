package tn.beecoders.elderly.service;

import org.springframework.data.jpa.domain.Specification;
import tn.beecoders.elderly.domain.Alert;
import tn.beecoders.elderly.domain.AlertPriority;

import java.time.LocalDateTime;

public final class AlertSpecifications {

    private AlertSpecifications() {
    }

    public static Specification<Alert> isResolvedEqual(Boolean resolved) {
        if (resolved == null) {
            return (root, q, cb) -> cb.conjunction();
        }
        return (root, q, cb) -> cb.equal(root.get("isResolved"), resolved);
    }

    public static Specification<Alert> priorityEqual(String priorityRaw) {
        if (priorityRaw == null || priorityRaw.isBlank()) {
            return (root, q, cb) -> cb.conjunction();
        }
        AlertPriority p = AlertPriority.valueOf(priorityRaw.trim().toUpperCase());
        return (root, q, cb) -> cb.equal(root.get("priority"), p);
    }

    public static Specification<Alert> timestampFrom(LocalDateTime from) {
        if (from == null) {
            return (root, q, cb) -> cb.conjunction();
        }
        return (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), from);
    }

    public static Specification<Alert> timestampTo(LocalDateTime to) {
        if (to == null) {
            return (root, q, cb) -> cb.conjunction();
        }
        return (root, q, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), to);
    }

    public static Specification<Alert> elderlyIdEqual(Long elderlyId) {
        if (elderlyId == null) {
            return (root, q, cb) -> cb.conjunction();
        }
        return (root, q, cb) -> cb.equal(root.join("elderlyPerson").get("id"), elderlyId);
    }
}
