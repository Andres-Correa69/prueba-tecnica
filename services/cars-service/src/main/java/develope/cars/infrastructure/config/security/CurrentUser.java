package develope.cars.infrastructure.config.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para parámetros de controlador que inyecta el usuario actualmente
 * autenticado. La resuelve {@link CurrentUserArgumentResolver}.
 *
 * <pre>{@code
 * @PostMapping
 * public ResponseEntity<CarResponse> create(@CurrentUser AuthenticatedUser user,
 *                                           @Valid @RequestBody CarRequest body) { ... }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {}
