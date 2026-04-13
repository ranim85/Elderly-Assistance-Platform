package tn.beecoders.elderly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.beecoders.elderly.domain.HealthRecord;

import java.util.List;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByElderlyPersonId(Long elderlyId);
}
