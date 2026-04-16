package develope.auth.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Binding tipado de las propiedades {@code security.jwt.*}.
 *
 * <p>Validado al arranque (@{@link Validated}) — un JWT_SECRET ausente
 * fallará la secuencia de boot con un mensaje claro, no con un NullPointer
 * aleatorio más tarde en el primer intento de login.</p>
 *
 * @param secret            clave HMAC para HS256 (compartida con cars-service).
 * @param issuer            valor del claim {@code iss}.
 * @param expirationMinutes tiempo de vida del token en minutos.
 */
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @NotBlank String issuer,
        @Positive long expirationMinutes
) {}
