package tn.beecoders.elderly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tn.beecoders.elderly.domain.CareStatus;
import tn.beecoders.elderly.domain.ElderlyPerson;

import java.util.List;
import java.util.Optional;

public interface ElderlyPersonRepository extends JpaRepository<ElderlyPerson, Long> {
    List<ElderlyPerson> findAllByCaregiver_Email(String email);
    long countByCaregiver_Email(String email);
    long countByCaregiver_Id(Long caregiverId);

    long countByCareStatus(CareStatus careStatus);

    long countByCaregiver_EmailAndCareStatus(String email, CareStatus careStatus);

    Optional<ElderlyPerson> findFirstByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    @Modifying
    @Query("update ElderlyPerson e set e.caregiver = null where e.caregiver is not null")
    void clearCaregiverReferences();
}
