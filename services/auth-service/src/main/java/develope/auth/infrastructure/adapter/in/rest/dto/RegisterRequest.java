package develope.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de la capa HTTP para {@code POST /auth/register}.
 *
 * <p>Las anotaciones de Bean Validation actúan como primera línea de defensa: los payloads
 * mal formados se rechazan antes de llegar a un caso de uso. La validación del dominio
 * (regex del nombre de usuario, fortaleza de la contraseña) corre de nuevo dentro del
 * caso de uso — defensa en profundidad.</p>
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 30) String username,
        @NotBlank @Size(min = 8, max = 100) String password
) {}
