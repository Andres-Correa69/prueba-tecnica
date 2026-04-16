package develope.cars.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Subconjunto de propiedades de JWT solo para validación: cars-service solo lee
 * tokens, nunca los emite. {@code secret} e {@code issuer} deben
 * coincidir exactamente con auth-service.
 */
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @NotBlank String issuer
) {}
