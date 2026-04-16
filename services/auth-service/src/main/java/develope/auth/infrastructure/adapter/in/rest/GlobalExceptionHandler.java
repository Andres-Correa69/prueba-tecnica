package develope.auth.infrastructure.adapter.in.rest;

import develope.auth.domain.exception.InvalidCredentialsException;
import develope.auth.domain.exception.UsernameAlreadyExistsException;
import develope.auth.infrastructure.adapter.in.rest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Traductor centralizado de errores HTTP.
 *
 * <p>Todos los endpoints REST delegan en casos de uso que pueden lanzar excepciones
 * de <em>dominio</em> (sin conocimiento de HTTP). El mapeo dominio → HTTP vive AQUÍ —
 * en un solo lugar, fácil de auditar.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> onDuplicate(UsernameAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "USERNAME_TAKEN", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> onInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> onBadDomainInput(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> onBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        // Traducir los errores por campo de Bean-Validation a nuestra forma uniforme.
        List<Map<String, String>> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
                .toList();
        ErrorResponse body = new ErrorResponse(
                "VALIDATION_ERROR",
                "request body is invalid",
                Instant.now(),
                req.getRequestURI(),
                fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> onAnythingElse(Exception ex, HttpServletRequest req) {
        // Excepciones desconocidas: NO filtrar stack traces ni errores de driver.
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "unexpected server error", req);
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                       HttpServletRequest req) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(code, message, Instant.now(), req.getRequestURI(), List.of()));
    }
}
