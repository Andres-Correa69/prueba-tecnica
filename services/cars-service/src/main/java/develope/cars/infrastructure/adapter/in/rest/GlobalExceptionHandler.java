package develope.cars.infrastructure.adapter.in.rest;

import develope.cars.domain.exception.CarNotFoundException;
import develope.cars.domain.exception.CarNotOwnedByUserException;
import develope.cars.domain.exception.PlacaAlreadyExistsException;
import develope.cars.infrastructure.adapter.in.rest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CarNotFoundException.class)
    public ResponseEntity<ErrorResponse> onNotFound(CarNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "CAR_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(CarNotOwnedByUserException.class)
    public ResponseEntity<ErrorResponse> onNotOwned(CarNotOwnedByUserException ex, HttpServletRequest req) {
        // Deliberadamente 404 y no 403 — evitar revelar la existencia de la fila.
        return build(HttpStatus.NOT_FOUND, "CAR_NOT_FOUND", "car not found", req);
    }

    @ExceptionHandler(PlacaAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> onPlacaConflict(PlacaAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "PLACA_TAKEN", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> onBadInput(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> onBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<Map<String, String>> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                "VALIDATION_ERROR", "request body is invalid",
                Instant.now(), req.getRequestURI(), fields));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> onAnythingElse(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "unexpected server error", req);
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
                                                       HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                code, message, Instant.now(), req.getRequestURI(), List.of()));
    }
}
