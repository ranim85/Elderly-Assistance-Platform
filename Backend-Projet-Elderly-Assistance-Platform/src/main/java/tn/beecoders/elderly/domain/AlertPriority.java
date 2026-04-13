package tn.beecoders.elderly.domain;

/**
 * Triage level for an alert, independent of {@link Alert.AlertType}.
 */
public enum AlertPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
