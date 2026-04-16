package develope.cars.application.usecase;

import develope.cars.application.port.in.DeleteCarUseCase;
import develope.cars.application.port.out.CarRepositoryPort;
import develope.cars.domain.exception.CarNotFoundException;
import develope.cars.domain.model.Car;
import develope.cars.domain.model.CarId;
import develope.cars.domain.model.OwnerId;

import java.util.Objects;
import java.util.UUID;

public final class DeleteCarService implements DeleteCarUseCase {

    private final CarRepositoryPort cars;

    public DeleteCarService(CarRepositoryPort cars) {
        this.cars = Objects.requireNonNull(cars);
    }

    @Override
    public void delete(UUID carId, UUID ownerId) {
        CarId id = CarId.of(carId);
        OwnerId owner = OwnerId.of(ownerId);

        Car car = cars.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new CarNotFoundException(id.value()));
        car.ensureOwnedBy(owner);
        cars.delete(car);
    }
}
