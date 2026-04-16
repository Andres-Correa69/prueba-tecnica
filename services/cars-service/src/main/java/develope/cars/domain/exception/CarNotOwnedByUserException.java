package develope.cars.domain.exception;

/**
 * Se lanza cuando una petición intenta actuar sobre un carro que no pertenece al
 * usuario autenticado.
 *
 * <p>Se mapea a HTTP 404 en lugar de 403 a propósito: responder con 403
 * le diría al atacante "este id existe, solo que no es tuyo", permitiendo
 * enumeración. El 404 no filtra nada.</p>
 */
public class CarNotOwnedByUserException extends RuntimeException {
    public CarNotOwnedByUserException() {
        super("car does not belong to the authenticated user");
    }
}
