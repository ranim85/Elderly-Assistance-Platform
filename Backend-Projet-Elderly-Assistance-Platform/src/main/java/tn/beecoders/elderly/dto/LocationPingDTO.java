package tn.beecoders.elderly.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationPingDTO {
    @NotNull
    private Long elderlyId;
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
}
