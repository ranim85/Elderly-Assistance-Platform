package tn.beecoders.elderly.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "elderly_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElderlySettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elderly_id", nullable = false, unique = true)
    private ElderlyPerson elderlyPerson;

    private Double homeLatitude;
    private Double homeLongitude;
    
    // Safety radius in meters
    private Double safeZoneRadius;
}
