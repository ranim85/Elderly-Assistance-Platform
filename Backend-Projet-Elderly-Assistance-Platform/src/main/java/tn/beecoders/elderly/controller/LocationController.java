package tn.beecoders.elderly.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.beecoders.elderly.dto.ElderlySettingsDTO;
import tn.beecoders.elderly.dto.LocationPingDTO;
import tn.beecoders.elderly.service.LocationService;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/ping")
    public ResponseEntity<Void> receiveLocationPing(@Valid @RequestBody LocationPingDTO ping) {
        locationService.processLocationPing(ping);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/settings/{elderlyId}")
    public ResponseEntity<ElderlySettingsDTO> getSettings(@PathVariable Long elderlyId) {
        return ResponseEntity.ok(locationService.getSettings(elderlyId));
    }

    @PutMapping("/settings")
    public ResponseEntity<ElderlySettingsDTO> updateSettings(@RequestBody ElderlySettingsDTO dto) {
        return ResponseEntity.ok(locationService.updateSettings(dto));
    }
}
