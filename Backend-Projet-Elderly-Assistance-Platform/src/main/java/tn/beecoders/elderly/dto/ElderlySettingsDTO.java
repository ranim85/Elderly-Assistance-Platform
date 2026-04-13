package tn.beecoders.elderly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElderlySettingsDTO {
    private Long id;
    private Long elderlyId;
    private Double homeLatitude;
    private Double homeLongitude;
    private Double safeZoneRadius;
}
