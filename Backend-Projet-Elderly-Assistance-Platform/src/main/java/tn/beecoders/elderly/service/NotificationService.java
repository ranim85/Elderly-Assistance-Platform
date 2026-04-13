package tn.beecoders.elderly.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.beecoders.elderly.dto.AlertDTO;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendAlertToCaregiver(String caregiverEmail, AlertDTO alertDTO) {
        if (caregiverEmail != null) {
            messagingTemplate.convertAndSendToUser(caregiverEmail, "/queue/alerts", alertDTO);
        } else {
            // Unassigned alerts broadcast to a global topic for admin/available caregivers
            messagingTemplate.convertAndSend("/topic/alerts", alertDTO);
        }
    }
}
