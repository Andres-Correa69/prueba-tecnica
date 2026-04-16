package develope.cars.application.port.in;

import develope.cars.domain.model.Car;

public interface UpdateCarUseCase {
    Car update(UpdateCarCommand command);
}
