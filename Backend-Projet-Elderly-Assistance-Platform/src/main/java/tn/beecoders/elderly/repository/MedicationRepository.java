package tn.beecoders.elderly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.beecoders.elderly.domain.Medication;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByElderlyPersonId(Long elderlyId);

    @Query("SELECT m FROM Medication m WHERE m.isTaken = false AND m.scheduledTime <= :currentTime")
    List<Medication> findOverdueMedications(@Param("currentTime") LocalDateTime currentTime);
}
