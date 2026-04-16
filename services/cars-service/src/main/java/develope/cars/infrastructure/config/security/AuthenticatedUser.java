package develope.cars.infrastructure.config.security;

import java.util.UUID;

/**
 * Contenedor inmutable para el principal autenticado.
 *
 * <p>Lo puebla {@code JwtAuthenticationFilter} tras una validación exitosa
 * del token. Se inyecta en los controladores vía la anotación {@code @CurrentUser}
 * + {@code CurrentUserArgumentResolver}.</p>
 */
public record AuthenticatedUser(UUID userId, String username) {}
