package develope.cars.domain.exception;

import java.util.UUID;

public class CarNotFoundException extends RuntimeException {
    public CarNotFoundException(UUID id) {
        super("car not found: " + id);
    }
}
