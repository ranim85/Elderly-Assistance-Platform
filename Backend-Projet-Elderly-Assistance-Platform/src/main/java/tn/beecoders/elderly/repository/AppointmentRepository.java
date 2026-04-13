package tn.beecoders.elderly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.beecoders.elderly.domain.Appointment;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByElderlyPersonId(Long elderlyId);
}
