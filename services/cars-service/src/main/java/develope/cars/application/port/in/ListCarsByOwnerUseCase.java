package develope.cars.application.port.in;

import develope.cars.application.port.out.DomainPage;
import develope.cars.application.port.out.PageRequest;
import develope.cars.domain.model.Car;

import java.util.UUID;

public interface ListCarsByOwnerUseCase {
    DomainPage<Car> list(UUID ownerId, CarFilter filter, PageRequest pageRequest);
}
