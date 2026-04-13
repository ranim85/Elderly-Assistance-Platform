package tn.beecoders.elderly.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import tn.beecoders.elderly.domain.*;
import tn.beecoders.elderly.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ElderlyPersonRepository elderlyPersonRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final AlertRepository alertRepository;
    private final MedicationRepository medicationRepository;
    private final ElderlySettingsRepository elderlySettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("[SEEDER] Wiping and Re-seeding database for demo consistency...");
        cleanupDatabase();
        seedDatabase();
        System.out.println("[SEEDER] Global Realistic Test Data injection complete.");
    }

    private void cleanupDatabase() {
        alertRepository.deleteAllInBatch();
        appointmentRepository.deleteAllInBatch();
        healthRecordRepository.deleteAllInBatch();
        medicationRepository.deleteAllInBatch();
        elderlySettingsRepository.deleteAllInBatch();
        // Break cyclical FKs (users.linked_elderly_person_id <->
        // elderly_persons.caregiver_id)
        userRepository.clearLinkedElderlyReferences();
        elderlyPersonRepository.clearCaregiverReferences();
        elderlyPersonRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    private void seedDatabase() {
        // 1. Create 1 Admin
        User admin = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email("admin@health.org")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        admin = userRepository.save(admin);

        // 2. Create 2 Caregivers
        User caregiver1 = User.builder()
                .firstName("Sarah")
                .lastName("Connor")
                .email("sarah.caregiver@health.org")
                .password(passwordEncoder.encode("care123"))
                .role(Role.CAREGIVER)
                .build();
        userRepository.save(caregiver1);

        User caregiver2 = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.caregiver@health.org")
                .password(passwordEncoder.encode("care123"))
                .role(Role.CAREGIVER)
                .build();
        userRepository.save(caregiver2);

        // 3. Create 15 Elderly Persons
        Random rand = new Random();
        List<ElderlyPerson> elderlyList = new ArrayList<>();

        String[] firstNames = { "Robert", "Mary", "James", "Patricia", "John", "Jennifer", "William", "Linda",
                "Richard", "Elizabeth", "Charles", "Susan", "Joseph", "Jessica", "Thomas" };
        String[] lastNames = { "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson" };
        String[] conditions = { "None", "Hypertension", "Diabetes Type 2", "Arthritis", "Asthma", "Dementia, Mild",
                "Hypertension, Osteoporosis" };

        CareStatus[] careStatuses = CareStatus.values();
        for (int i = 0; i < 15; i++) {
            ElderlyPerson person = ElderlyPerson.builder()
                    .firstName(firstNames[i])
                    .lastName(lastNames[i])
                    .dateOfBirth(LocalDate.of(1930 + rand.nextInt(20), 1 + rand.nextInt(12), 1 + rand.nextInt(28)))
                    .address(rand.nextInt(9999) + " Spring Ave, Suite " + rand.nextInt(20))
                    .medicalConditions(conditions[rand.nextInt(conditions.length)])
                    .careStatus(careStatuses[rand.nextInt(careStatuses.length)])
                    .caregiver(i % 2 == 0 ? caregiver1 : caregiver2) // Distribute between the 2 caregivers
                    .build();
            elderlyList.add(elderlyPersonRepository.save(person));
        }

        // 4. Create Health Records
        for (ElderlyPerson elderly : elderlyList) {
            int recordCount = 1 + rand.nextInt(3);
            for (int i = 0; i < recordCount; i++) {
                int sys = 110 + rand.nextInt(40);
                int dia = 70 + rand.nextInt(20);
                HealthRecord record = HealthRecord.builder()
                        .elderlyPerson(elderly)
                        .bloodPressure(sys + "/" + dia)
                        .heartRate(60 + rand.nextInt(40))
                        .bloodSugar(80.0 + (rand.nextDouble() * 40))
                        .recordedAt(LocalDateTime.now().minusDays(rand.nextInt(30)).minusHours(rand.nextInt(24)))
                        .build();
                healthRecordRepository.save(record);
            }
        }

        // 5. Create Appointments
        for (int i = 0; i < 10; i++) {
            Appointment appt = Appointment.builder()
                    .elderlyPerson(elderlyList.get(rand.nextInt(elderlyList.size())))
                    .doctorName("Dr. " + lastNames[rand.nextInt(lastNames.length)])
                    .purpose("Regular Checkup")
                    .appointmentDate(LocalDateTime.now().plusDays(rand.nextInt(14)).plusHours(8 + rand.nextInt(8)))
                    .status(rand.nextBoolean() ? Appointment.AppointmentStatus.SCHEDULED
                            : Appointment.AppointmentStatus.COMPLETED)
                    .build();
            appointmentRepository.save(appt);
        }

        // 6. Create Urgent Alerts
        Alert.AlertType[] alertTypes = Alert.AlertType.values();
        AlertPriority[] priorities = AlertPriority.values();
        for (int i = 0; i < 6; i++) {
            boolean resolved = i > 2;
            LocalDateTime ts = LocalDateTime.now().minusMinutes(rand.nextInt(1440));
            Alert alert = Alert.builder()
                    .elderlyPerson(elderlyList.get(i))
                    .alertType(alertTypes[rand.nextInt(alertTypes.length)])
                    .priority(priorities[rand.nextInt(priorities.length)])
                    .description("Automated systematic alert triggered.")
                    .timestamp(ts)
                    .isResolved(resolved)
                    .resolvedAt(resolved ? ts.plusMinutes(5) : null)
                    .resolvedBy(resolved ? admin : null)
                    .build();
            alertRepository.save(alert);
        }
    }
}
