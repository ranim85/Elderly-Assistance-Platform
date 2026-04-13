package tn.beecoders.elderly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tn.beecoders.elderly.domain.Alert;

public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {
    long countByIsResolvedFalse();
    long countByIsResolvedFalseAndElderlyPerson_Caregiver_Email(String email);
    boolean existsByElderlyPersonIdAndDescriptionAndIsResolvedFalse(Long elderlyId, String description);
    java.util.List<Alert> findByElderlyPersonId(Long elderlyId);
}
