package tn.beecoders.elderly.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "medications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String dosage;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    private boolean isTaken;

    private LocalDateTime timeTaken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elderly_id", nullable = false)
    private ElderlyPerson elderlyPerson;
}
