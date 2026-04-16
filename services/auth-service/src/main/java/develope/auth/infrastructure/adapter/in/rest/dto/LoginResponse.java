package develope.auth.infrastructure.adapter.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Cuerpo de respuesta de {@code POST /auth/login}.
 *
 * @param token     JWT que se adjunta como {@code Authorization: Bearer <token>}.
 * @param userId    id del usuario (también codificado en el claim {@code sub} del JWT).
 * @param username  nombre de usuario para mostrar en la UI.
 * @param expiresAt timestamp absoluto de expiración (UTC, ISO-8601).
 */
public record LoginResponse(String token, UUID userId, String username, Instant expiresAt) {}
