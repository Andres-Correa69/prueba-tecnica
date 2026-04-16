package develope.cars.domain.exception;

public class PlacaAlreadyExistsException extends RuntimeException {
    public PlacaAlreadyExistsException(String placa) {
        super("placa already registered: " + placa);
    }
}
