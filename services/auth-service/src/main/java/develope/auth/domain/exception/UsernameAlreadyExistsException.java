package develope.auth.domain.exception;

/**
 * Lanzada por el caso de uso de registro cuando el nombre de usuario objetivo ya está tomado.
 *
 * <p>Se mapea a HTTP 409 (Conflict) en el adaptador REST. El dominio en sí no
 * conoce — ni le importa — HTTP; el mapeo vive en
 * {@code GlobalExceptionHandler}.</p>
 */
public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("username already exists: " + username);
    }
}
