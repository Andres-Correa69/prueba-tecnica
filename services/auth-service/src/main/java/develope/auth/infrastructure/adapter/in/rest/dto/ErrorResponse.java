package develope.auth.infrastructure.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Envoltorio de error uniforme retornado por cada endpoint en caso de fallo.
 *
 * <p>Mantener una única forma para los errores hace trivial al frontend: una
 * sola rama en el interceptor de axios maneja todos ellos.</p>
 *
 * @param code        código corto legible por máquina, por ejemplo {@code USERNAME_TAKEN}.
 * @param message     mensaje legible por humanos (seguro de mostrar en la UI).
 * @param timestamp   timestamp UTC del error.
 * @param path        la URI de la petición que falló.
 * @param fieldErrors mensajes por campo de Bean-Validation, cuando el error fue validación 400.
 */
public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String path,
        List<Map<String, String>> fieldErrors
) {}
