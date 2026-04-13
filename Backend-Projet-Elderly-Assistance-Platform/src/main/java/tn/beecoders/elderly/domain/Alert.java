package tn.beecoders.elderly.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AlertType alertType;

    /** Nullable for legacy rows; API maps null to {@link AlertPriority#MEDIUM}. */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private AlertPriority priority;

    private String description;

    @Column(name = "alert_timestamp")
    private LocalDateTime timestamp;
    private boolean isResolved;

    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elderly_id", nullable = false)
    private ElderlyPerson elderlyPerson;
    
    public enum AlertType {
        SOS, MEDICAL_EMERGENCY, FALL_DETECTED, WANDERING_EMERGENCY
    }
}
