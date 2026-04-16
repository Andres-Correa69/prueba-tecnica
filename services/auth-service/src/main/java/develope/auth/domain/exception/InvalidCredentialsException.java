package develope.auth.domain.exception;

/**
 * Lanzada por el caso de uso de login cuando el usuario o la contraseña son incorrectos.
 *
 * <p>El mensaje es intencionalmente genérico ("invalid credentials") — nunca
 * debemos indicarle al llamador si fue el usuario o la contraseña lo que
 * estaba mal, eso facilitaría ataques de enumeración de cuentas.</p>
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("invalid credentials");
    }
}
