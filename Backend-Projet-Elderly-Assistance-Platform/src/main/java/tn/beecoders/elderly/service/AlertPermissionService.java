package tn.beecoders.elderly.service;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tn.beecoders.elderly.domain.Alert;
import tn.beecoders.elderly.domain.ElderlyPerson;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.repository.UserRepository;

import java.util.Locale;

/**
 * Authorization rules for alert-related mutations and list scoping.
 */
@Service
@RequiredArgsConstructor
public class AlertPermissionService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_CAREGIVER = "ROLE_CAREGIVER";
    private static final String ROLE_FAMILY_MEMBER = "ROLE_FAMILY_MEMBER";
    private static final String ROLE_ELDERLY = "ROLE_ELDERLY";

    private final UserRepository userRepository;

    /**
     * Row-level scope for alert queries: admin sees all; caregiver sees assigned elderly;
     * family member sees linked elderly only; elderly sees rows matching their name (same as linked profile pattern).
     */
    public Specification<Alert> alertScope(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return (root, q, cb) -> cb.disjunction();
        }
        if (isAdmin(authentication)) {
            return (root, q, cb) -> cb.conjunction();
        }

        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return (root, q, cb) -> cb.disjunction();
        }

        if (hasRole(authentication, ROLE_FAMILY_MEMBER)) {
            if (user.getLinkedElderlyPerson() == null) {
                return (root, q, cb) -> cb.disjunction();
            }
            Long lid = user.getLinkedElderlyPerson().getId();
            return (root, q, cb) -> cb.equal(root.join("elderlyPerson", JoinType.INNER).get("id"), lid);
        }

        if (hasRole(authentication, ROLE_ELDERLY)) {
            String fn = user.getFirstName();
            String ln = user.getLastName();
            return (root, q, cb) -> {
                var ep = root.join("elderlyPerson", JoinType.INNER);
                return cb.and(
                        cb.equal(cb.lower(ep.get("firstName")), fn.toLowerCase(Locale.ROOT)),
                        cb.equal(cb.lower(ep.get("lastName")), ln.toLowerCase(Locale.ROOT))
                );
            };
        }

        if (hasRole(authentication, ROLE_CAREGIVER)) {
            return (root, q, cb) -> cb.equal(
                    root.join("elderlyPerson", JoinType.INNER).join("caregiver", JoinType.INNER).get("email"),
                    authentication.getName()
            );
        }

        return (root, q, cb) -> cb.disjunction();
    }

    public void assertCanResolve(Authentication authentication, Alert alert) {
        assertAdminOrOwningCaregiver(authentication, alert, "Only an administrator or the assigned caregiver can resolve this alert");
    }

    public void assertCanCreateAlert(Authentication authentication, ElderlyPerson elderly) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        if (isAdmin(authentication)) {
            return;
        }
        User caregiver = elderly.getCaregiver();
        if (caregiver == null) {
            throw new AccessDeniedException("Assign a caregiver to this elderly person before caregivers can create alerts.");
        }
        if (!caregiver.getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("Only an administrator or the assigned caregiver can create alerts for this elderly person.");
        }
    }

    public void assertCanModifyAlert(Authentication authentication, Alert alert) {
        assertAdminOrOwningCaregiver(authentication, alert,
                "Only an administrator or the assigned caregiver can modify this alert");
    }

    private void assertAdminOrOwningCaregiver(Authentication authentication, Alert alert, String denialMessage) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        if (isAdmin(authentication)) {
            return;
        }
        User caregiver = alert.getElderlyPerson().getCaregiver();
        if (caregiver == null) {
            throw new AccessDeniedException("This alert is not assigned to a caregiver");
        }
        String principalEmail = authentication.getName();
        if (principalEmail == null || !principalEmail.equalsIgnoreCase(caregiver.getEmail())) {
            throw new AccessDeniedException(denialMessage);
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, ROLE_ADMIN);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> role.equals(a.getAuthority()));
    }
}
