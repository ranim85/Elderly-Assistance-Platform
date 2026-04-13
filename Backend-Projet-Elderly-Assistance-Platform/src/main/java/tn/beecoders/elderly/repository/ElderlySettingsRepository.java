package tn.beecoders.elderly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.beecoders.elderly.domain.ElderlySettings;
import java.util.Optional;

public interface ElderlySettingsRepository extends JpaRepository<ElderlySettings, Long> {
    Optional<ElderlySettings> findByElderlyPersonId(Long elderlyId);
}
