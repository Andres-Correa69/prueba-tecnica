package develope.cars.application.usecase;

import develope.cars.application.port.in.CreateCarCommand;
import develope.cars.application.port.in.CreateCarUseCase;
import develope.cars.application.port.out.CarRepositoryPort;
import develope.cars.domain.exception.PlacaAlreadyExistsException;
import develope.cars.domain.model.Car;
import develope.cars.domain.model.OwnerId;
import develope.cars.domain.vo.Anio;
import develope.cars.domain.vo.Color;
import develope.cars.domain.vo.FotoUrl;
import develope.cars.domain.vo.Marca;
import develope.cars.domain.vo.Modelo;
import develope.cars.domain.vo.Placa;

import java.util.Objects;
import java.util.Optional;

public final class CreateCarService implements CreateCarUseCase {

    private final CarRepositoryPort cars;

    public CreateCarService(CarRepositoryPort cars) {
        this.cars = Objects.requireNonNull(cars);
    }

    @Override
    public Car create(CreateCarCommand cmd) {
        Objects.requireNonNull(cmd, "command");

        // Construir todos los VOs primero — cada entrada se valida antes de cualquier llamada a la BD.
        Placa placa = Placa.of(cmd.placa());
        if (cars.existsByPlaca(placa)) {
            throw new PlacaAlreadyExistsException(placa.value());
        }

        Car car = Car.create(
                OwnerId.of(cmd.ownerId()),
                Marca.of(cmd.marca()),
                Modelo.of(cmd.modelo()),
                Anio.of(cmd.anio()),
                placa,
                Color.of(cmd.color()),
                FotoUrl.ofNullable(cmd.fotoUrl())
        );
        cars.save(car);
        return car;
    }
}
