package tn.beecoders.elderly.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bloodPressure;
    private Integer heartRate;
    private Double bloodSugar;
    private LocalDateTime recordedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elderly_id", nullable = false)
    private ElderlyPerson elderlyPerson;
}
