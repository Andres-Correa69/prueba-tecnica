package develope.cars.application.usecase;

import develope.cars.application.port.in.GetCarUseCase;
import develope.cars.application.port.out.CarRepositoryPort;
import develope.cars.domain.exception.CarNotFoundException;
import develope.cars.domain.model.Car;
import develope.cars.domain.model.CarId;
import develope.cars.domain.model.OwnerId;

import java.util.Objects;
import java.util.UUID;

public final class GetCarService implements GetCarUseCase {

    private final CarRepositoryPort cars;

    public GetCarService(CarRepositoryPort cars) {
        this.cars = Objects.requireNonNull(cars);
    }

    @Override
    public Car getByIdForOwner(UUID carId, UUID ownerId) {
        CarId id = CarId.of(carId);
        OwnerId owner = OwnerId.of(ownerId);
        return cars.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new CarNotFoundException(id.value()));
    }
}
