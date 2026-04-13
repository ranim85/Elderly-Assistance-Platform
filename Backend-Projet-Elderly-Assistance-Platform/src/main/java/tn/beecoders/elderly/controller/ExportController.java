package tn.beecoders.elderly.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.beecoders.elderly.domain.ElderlyPerson;
import tn.beecoders.elderly.exception.ResourceNotFoundException;
import tn.beecoders.elderly.repository.ElderlyPersonRepository;
import tn.beecoders.elderly.service.PdfExportService;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ExportController {

    private final PdfExportService pdfExportService;
    private final ElderlyPersonRepository elderlyPersonRepository;

    @GetMapping("/elderly/{id}/export-pdf")
    public ResponseEntity<InputStreamResource> exportPdf(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        ElderlyPerson elderly = elderlyPersonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Elderly person not found with id " + id));

        // Default: Last 30 days if no range provided
        if (from == null && to == null) {
            from = LocalDateTime.now().minusDays(30);
            to = LocalDateTime.now();
        }

        ByteArrayInputStream bis = pdfExportService.generateElderlyMedicalReport(elderly, from, to);
        HttpHeaders headers = new HttpHeaders();
        // Use attachment so browser triggers download
        headers.add("Content-Disposition", "attachment; filename=medical_report_" + elderly.getLastName() + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}
