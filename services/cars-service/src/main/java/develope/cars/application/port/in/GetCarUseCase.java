package develope.cars.application.port.in;

import develope.cars.domain.model.Car;

import java.util.UUID;

public interface GetCarUseCase {
    Car getByIdForOwner(UUID carId, UUID ownerId);
}
