package tn.beecoders.elderly.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String doctorName;
    private String purpose;
    private LocalDateTime appointmentDate;
    
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elderly_id", nullable = false)
    private ElderlyPerson elderlyPerson;
    
    public enum AppointmentStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }
}
