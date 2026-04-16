package develope.cars.infrastructure.adapter.in.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Forma uniforme de error que refleja el ErrorResponse de {@code auth-service}.
 *
 * <p>Copiado y pegado intencionalmente (sin módulo compartido) — el principio
 * de independencia de los microservicios dice "preferir la duplicación sobre el
 * acoplamiento prematuro". Si tuviéramos 10 servicios, lo reconsideraríamos y
 * publicaríamos un artefacto {@code common-api} en Maven/Gradle.</p>
 */
public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String path,
        List<Map<String, String>> fieldErrors
) {}
