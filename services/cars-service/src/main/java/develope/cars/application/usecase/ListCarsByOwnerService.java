package develope.cars.application.usecase;

import develope.cars.application.port.in.CarFilter;
import develope.cars.application.port.in.ListCarsByOwnerUseCase;
import develope.cars.application.port.out.CarRepositoryPort;
import develope.cars.application.port.out.DomainPage;
import develope.cars.application.port.out.PageRequest;
import develope.cars.domain.model.Car;
import develope.cars.domain.model.OwnerId;

import java.util.Objects;
import java.util.UUID;

public final class ListCarsByOwnerService implements ListCarsByOwnerUseCase {

    private final CarRepositoryPort cars;

    public ListCarsByOwnerService(CarRepositoryPort cars) {
        this.cars = Objects.requireNonNull(cars);
    }

    @Override
    public DomainPage<Car> list(UUID ownerId, CarFilter filter, PageRequest pageRequest) {
        return cars.findByOwner(
                OwnerId.of(ownerId),
                filter == null ? CarFilter.empty() : filter,
                pageRequest);
    }
}
