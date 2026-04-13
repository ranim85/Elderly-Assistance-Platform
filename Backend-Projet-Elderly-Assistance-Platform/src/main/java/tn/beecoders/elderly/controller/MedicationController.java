package tn.beecoders.elderly.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tn.beecoders.elderly.domain.Medication;
import tn.beecoders.elderly.dto.MedicationCreateRequest;
import tn.beecoders.elderly.dto.MedicationDTO;
import tn.beecoders.elderly.exception.ResourceNotFoundException;
import tn.beecoders.elderly.repository.ElderlyPersonRepository;
import tn.beecoders.elderly.repository.MedicationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationRepository medicationRepository;
    private final ElderlyPersonRepository elderlyPersonRepository;

    @GetMapping("/elderly/{elderlyId}")
    public ResponseEntity<List<MedicationDTO>> getMedicationsByElderly(@PathVariable Long elderlyId) {
        var meds = medicationRepository.findByElderlyPersonId(elderlyId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(meds);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<MedicationDTO> createMedication(@Valid @RequestBody MedicationCreateRequest request) {
        var elderly = elderlyPersonRepository.findById(request.elderlyPersonId())
                .orElseThrow(() -> new ResourceNotFoundException("Elderly person not found"));

        Medication med = Medication.builder()
                .elderlyPerson(elderly)
                .name(request.name())
                .dosage(request.dosage())
                .scheduledTime(request.scheduledTime())
                .isTaken(false)
                .build();
        
        Medication saved = medicationRepository.save(med);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(saved));
    }

    @PutMapping("/{id}/take")
    @Transactional
    public ResponseEntity<MedicationDTO> markAsTaken(@PathVariable Long id) {
        return medicationRepository.findById(id).map(med -> {
            med.setTaken(true);
            med.setTimeTaken(LocalDateTime.now());
            return ResponseEntity.ok(mapToDTO(medicationRepository.save(med)));
        }).orElse(ResponseEntity.notFound().build());
    }

    private MedicationDTO mapToDTO(Medication med) {
        return MedicationDTO.builder()
                .id(med.getId())
                .name(med.getName())
                .dosage(med.getDosage())
                .scheduledTime(med.getScheduledTime())
                .isTaken(med.isTaken())
                .timeTaken(med.getTimeTaken())
                .elderlyPersonId(med.getElderlyPerson().getId())
                .build();
    }
}
