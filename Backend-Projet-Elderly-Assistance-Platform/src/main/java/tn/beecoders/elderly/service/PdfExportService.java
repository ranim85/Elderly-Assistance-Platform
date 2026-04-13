package tn.beecoders.elderly.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.beecoders.elderly.domain.ElderlyPerson;
import tn.beecoders.elderly.dto.TimelineEventDTO;
import tn.beecoders.elderly.repository.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final AlertRepository alertRepository;
    private final AppointmentRepository appointmentRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final MedicationRepository medicationRepository;

    public ByteArrayInputStream generateElderlyMedicalReport(ElderlyPerson elderly, LocalDateTime from, LocalDateTime to) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font tableCellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("Medical History Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);
            
            Paragraph subtitle = new Paragraph("Elderly Assistance Platform", regularFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(30);
            document.add(subtitle);

            // Patient Info Block
            document.add(new Paragraph("Patient Profile", subtitleFont));
            document.add(new Paragraph("Name: " + elderly.getFirstName() + " " + elderly.getLastName(), regularFont));
            document.add(new Paragraph("Date of Birth: " + elderly.getDateOfBirth(), regularFont));
            document.add(new Paragraph("Medical Conditions: " + (elderly.getMedicalConditions() != null ? elderly.getMedicalConditions() : "None"), regularFont));
            document.add(new Paragraph("Generated at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), regularFont));
            document.add(new Paragraph(" ", regularFont));

            // Fetch Data
            List<TimelineEventDTO> timeline = fetchTimelineEvents(elderly.getId());
            
            // Filter by date
            List<TimelineEventDTO> filtered = timeline.stream().filter(e -> {
                boolean afterFrom = from == null || !e.date().isBefore(from);
                boolean beforeTo = to == null || !e.date().isAfter(to);
                return afterFrom && beforeTo;
            }).collect(Collectors.toList());

            Paragraph historyTitle = new Paragraph("Chronological History (" + filtered.size() + " events)", subtitleFont);
            historyTitle.setSpacingAfter(10);
            document.add(historyTitle);

            // Create Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2f, 2.5f, 4f}); // Fixed floating point widths correctly

            String[] headers = {"Date", "Category", "Event", "Details"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, tableHeaderFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new java.awt.Color(240, 240, 240));
                cell.setPadding(6);
                table.addCell(cell);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
            for (TimelineEventDTO event : filtered) {
                PdfPCell dateCell = new PdfPCell(new Phrase(event.date().format(dtf), tableCellFont));
                dateCell.setPadding(5);
                table.addCell(dateCell);

                PdfPCell typeCell = new PdfPCell(new Phrase(event.eventType().replace("_", " "), tableCellFont));
                typeCell.setPadding(5);
                table.addCell(typeCell);
                
                PdfPCell titleCell = new PdfPCell(new Phrase(event.title(), tableCellFont));
                titleCell.setPadding(5);
                table.addCell(titleCell);
                
                PdfPCell descCell = new PdfPCell(new Phrase(event.description(), tableCellFont));
                descCell.setPadding(5);
                table.addCell(descCell);
            }

            document.add(table);
            document.close();
            
        } catch (DocumentException ex) {
            throw new RuntimeException("Error generating PDF", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private List<TimelineEventDTO> fetchTimelineEvents(Long elderlyId) {
        List<TimelineEventDTO> timeline = new ArrayList<>();
        alertRepository.findByElderlyPersonId(elderlyId).forEach(a -> timeline.add(TimelineEventDTO.builder()
                .id("AL-" + a.getId()).date(a.getTimestamp()).eventType("ALERT").title(a.getAlertType().name()).description(a.getDescription()).severity("CRITICAL").build()));
        appointmentRepository.findByElderlyPersonId(elderlyId).forEach(ap -> timeline.add(TimelineEventDTO.builder()
                .id("AP-" + ap.getId()).date(ap.getAppointmentDate()).eventType("APPOINTMENT").title("Medical Appointment").description("Doc: " + ap.getDoctorName() + "\n" + ap.getPurpose()).severity("NORMAL").build()));
        healthRecordRepository.findByElderlyPersonId(elderlyId).forEach(h -> timeline.add(TimelineEventDTO.builder()
                .id("HR-" + h.getId()).date(h.getRecordedAt()).eventType("HEALTH_RECORD").title("Vitals Checked").description("BP: " + h.getBloodPressure() + "\nHR: " + h.getHeartRate()).severity("INFO").build()));
        medicationRepository.findByElderlyPersonId(elderlyId).forEach(m -> timeline.add(TimelineEventDTO.builder()
                .id("MD-" + m.getId()).date(m.isTaken() ? m.getTimeTaken() : m.getScheduledTime()).eventType("MEDICATION").title(m.getName()).description("Dosage: " + m.getDosage() + "\nState: " + (m.isTaken() ? "Taken" : "Missed")).severity(m.isTaken() ? "SUCCESS" : "WARNING").build()));
        timeline.sort(Comparator.comparing(TimelineEventDTO::date).reversed());
        return timeline;
    }
}
