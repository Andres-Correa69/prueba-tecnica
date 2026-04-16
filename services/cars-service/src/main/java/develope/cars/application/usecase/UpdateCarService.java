package develope.cars.application.usecase;

import develope.cars.application.port.in.UpdateCarCommand;
import develope.cars.application.port.in.UpdateCarUseCase;
import develope.cars.application.port.out.CarRepositoryPort;
import develope.cars.domain.exception.CarNotFoundException;
import develope.cars.domain.exception.PlacaAlreadyExistsException;
import develope.cars.domain.model.Car;
import develope.cars.domain.model.CarId;
import develope.cars.domain.model.OwnerId;
import develope.cars.domain.vo.Anio;
import develope.cars.domain.vo.Color;
import develope.cars.domain.vo.FotoUrl;
import develope.cars.domain.vo.Marca;
import develope.cars.domain.vo.Modelo;
import develope.cars.domain.vo.Placa;

import java.util.Objects;

public final class UpdateCarService implements UpdateCarUseCase {

    private final CarRepositoryPort cars;

    public UpdateCarService(CarRepositoryPort cars) {
        this.cars = Objects.requireNonNull(cars);
    }

    @Override
    public Car update(UpdateCarCommand cmd) {
        Objects.requireNonNull(cmd, "command");

        CarId carId = CarId.of(cmd.carId());
        OwnerId owner = OwnerId.of(cmd.ownerId());

        // Verificación de propiedad #1: el repositorio solo devuelve el carro si
        // pertenece al dueño que hace la petición. Un no-dueño recibe 404, nunca 403.
        Car existing = cars.findByIdAndOwner(carId, owner)
                .orElseThrow(() -> new CarNotFoundException(carId.value()));

        // Verificación de propiedad #2: doble chequeo (defensa en profundidad) — el dominio re-verifica.
        // Si alguna vez se cambia la consulta de persistencia a "findById", esta
        // línea sigue previniendo IDOR.
        existing.ensureOwnedBy(owner);

        Placa newPlaca = Placa.of(cmd.placa());

        // Permitir mantener la misma placa; solo rechazar si la placa ahora
        // colisiona con un carro DIFERENTE.
        if (!existing.placa().equals(newPlaca) && cars.existsByPlacaAndIdNot(newPlaca, carId)) {
            throw new PlacaAlreadyExistsException(newPlaca.value());
        }

        Car updated = existing.withUpdates(
                Marca.of(cmd.marca()),
                Modelo.of(cmd.modelo()),
                Anio.of(cmd.anio()),
                newPlaca,
                Color.of(cmd.color()),
                FotoUrl.ofNullable(cmd.fotoUrl())
        );
        cars.save(updated);
        return updated;
    }
}
