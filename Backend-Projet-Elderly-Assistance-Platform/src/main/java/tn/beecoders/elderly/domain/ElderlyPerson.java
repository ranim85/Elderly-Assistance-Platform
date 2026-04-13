package tn.beecoders.elderly.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "elderly_persons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElderlyPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String address;
    private String medicalConditions;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "care_status", length = 32, nullable = false)
    private CareStatus careStatus = CareStatus.STABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id")
    private User caregiver;
}
