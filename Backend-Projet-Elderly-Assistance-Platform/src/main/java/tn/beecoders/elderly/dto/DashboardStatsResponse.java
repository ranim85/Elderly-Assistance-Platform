package tn.beecoders.elderly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsResponse {
    private long totalAssisted;
    private long activeCaregivers;
    private long urgentAlerts;
    private long elderlyStable;
    private long elderlyWarning;
    private long elderlyCritical;
}
