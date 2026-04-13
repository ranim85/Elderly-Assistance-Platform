package tn.beecoders.elderly.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Maximum number of {@link tn.beecoders.elderly.domain.ElderlyPerson} records per caregiver.
 * Configure with {@code app.caregiver.max-elderly} (see {@code application.yml}).
 */
@ConfigurationProperties(prefix = "app.caregiver")
public record CaregiverCapacityProperties(int maxElderly) {}
