package develope.auth.application.port.in;

import java.time.Instant;
import java.util.UUID;

/**
 * Resultado retornado por {@link LoginUseCase}.
 *
 * <p>Contiene deliberadamente el token + metadatos que el frontend necesita para
 * hacer peticiones autenticadas. NO es un DTO — el adaptador REST lo envuelve en
 * un record de respuesta JSON — pero los campos coinciden porque el frontend
 * necesita todos ellos.</p>
 *
 * @param token      el JWT firmado (HS256).
 * @param userId     id del usuario autenticado.
 * @param username   nombre de usuario para mostrar en la UI.
 * @param expiresAt  timestamp absoluto de expiración (UTC). Permite al frontend
 *                   redirigir proactivamente a {@code /login} cuando el token
 *                   ha expirado, sin esperar a un 401.
 */
public record AuthResult(String token, UUID userId, String username, Instant expiresAt) {}
