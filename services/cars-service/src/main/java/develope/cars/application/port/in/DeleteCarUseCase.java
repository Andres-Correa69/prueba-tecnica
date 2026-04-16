package develope.cars.application.port.in;

import java.util.UUID;

public interface DeleteCarUseCase {
    /** Elimina el carro si pertenece al dueño indicado. */
    void delete(UUID carId, UUID ownerId);
}
