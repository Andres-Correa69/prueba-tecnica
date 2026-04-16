package develope.auth.application.port.out;

import develope.auth.domain.vo.PasswordHash;

/**
 * Puerto de salida: hashea contraseñas planas y verifica candidatas.
 *
 * <p>Mover el hasheo de contraseñas detrás de un puerto nos permite cambiar BCrypt por Argon2
 * (o aplicar un pepper al input, o limitar la tasa mediante un decorador rate-limiter) sin
 * tocar los casos de uso. El adaptador actual envuelve el
 * {@code BCryptPasswordEncoder} de Spring Security.</p>
 */
public interface PasswordHasherPort {

    /** Hashea una contraseña en texto plano con el algoritmo + costo actuales. */
    PasswordHash hash(String rawPassword);

    /** Comparación en tiempo constante de una candidata contra un hash almacenado. */
    boolean matches(String rawPassword, PasswordHash stored);
}
