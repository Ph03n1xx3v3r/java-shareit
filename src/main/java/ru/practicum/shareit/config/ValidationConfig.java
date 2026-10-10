package ru.practicum.shareit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class ValidationConfig {
    private static final Duration BOOKING_VALIDATION_CLOCK_SKEW = Duration.ofSeconds(5);

    @Bean
    public LocalValidatorFactoryBean defaultValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setConfigurationInitializer(configuration -> configuration.clockProvider(
                () -> Clock.offset(Clock.systemDefaultZone(), BOOKING_VALIDATION_CLOCK_SKEW.negated())
        ));
        return validator;
    }
}
