package tn.beecoders.elderly.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.beecoders.elderly.config.CaregiverCapacityProperties;
import tn.beecoders.elderly.domain.ElderlyPerson;
import tn.beecoders.elderly.domain.Role;
import tn.beecoders.elderly.domain.User;
import tn.beecoders.elderly.exception.BadRequestException;
import tn.beecoders.elderly.repository.ElderlyPersonRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CaregiverAssignmentService {

    private final ElderlyPersonRepository elderlyPersonRepository;
    private final CaregiverCapacityProperties caregiverCapacityProperties;

    /**
     * Validates capacity and sets {@link ElderlyPerson#setCaregiver(User)} (caller persists).
     */
    public void assignCaregiver(ElderlyPerson elderly, User caregiver) {
        if (caregiver.getRole() != Role.CAREGIVER) {
            throw new BadRequestException("Only a user with role CAREGIVER can be assigned as caregiver.");
        }
        Long caregiverId = caregiver.getId();
        boolean alreadyAssignedToThisCaregiver = elderly.getCaregiver() != null
                && Objects.equals(elderly.getCaregiver().getId(), caregiverId);
        if (!alreadyAssignedToThisCaregiver) {
            long occupied = elderlyPersonRepository.countByCaregiver_Id(caregiverId);
            int max = caregiverCapacityProperties.maxElderly();
            if (occupied >= max) {
                throw new BadRequestException("This caregiver already has the maximum caseload (" + max + " elderly persons).");
            }
        }
        elderly.setCaregiver(caregiver);
    }

    /** Before persisting a new {@link ElderlyPerson} assigned to this caregiver. */
    public void validateCapacityForNewElderly(User caregiver) {
        if (caregiver.getRole() != Role.CAREGIVER) {
            throw new BadRequestException("Only a user with role CAREGIVER can be assigned as caregiver.");
        }
        long occupied = elderlyPersonRepository.countByCaregiver_Id(caregiver.getId());
        int max = caregiverCapacityProperties.maxElderly();
        if (occupied >= max) {
            throw new BadRequestException("This caregiver already has the maximum caseload (" + max + " elderly persons).");
        }
    }
}
