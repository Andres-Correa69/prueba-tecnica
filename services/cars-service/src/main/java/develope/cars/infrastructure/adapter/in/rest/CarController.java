package develope.cars.infrastructure.adapter.in.rest;

import develope.cars.application.port.in.CarFilter;
import develope.cars.application.port.in.CreateCarCommand;
import develope.cars.application.port.in.CreateCarUseCase;
import develope.cars.application.port.in.DeleteCarUseCase;
import develope.cars.application.port.in.GetCarUseCase;
import develope.cars.application.port.in.ListCarsByOwnerUseCase;
import develope.cars.application.port.in.UpdateCarCommand;
import develope.cars.application.port.in.UpdateCarUseCase;
import develope.cars.application.port.out.DomainPage;
import develope.cars.application.port.out.PageRequest;
import develope.cars.domain.model.Car;
import develope.cars.infrastructure.adapter.in.rest.dto.CarListResponse;
import develope.cars.infrastructure.adapter.in.rest.dto.CarRequest;
import develope.cars.infrastructure.adapter.in.rest.dto.CarResponse;
import develope.cars.infrastructure.config.security.AuthenticatedUser;
import develope.cars.infrastructure.config.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST del CRUD de carros.
 *
 * <p>Cada endpoint delega a un caso de uso. El {@code ownerId} siempre se
 * extrae de {@code @CurrentUser} (es decir, del JWT), nunca del cuerpo de la
 * petición — esta es la línea de defensa más importante contra que
 * los usuarios modifiquen los carros de otros.</p>
 */
@RestController
@RequestMapping("/cars")
public class CarController {

    private final CreateCarUseCase createCar;
    private final UpdateCarUseCase updateCar;
    private final DeleteCarUseCase deleteCar;
    private final GetCarUseCase getCar;
    private final ListCarsByOwnerUseCase listCars;

    public CarController(CreateCarUseCase createCar,
                         UpdateCarUseCase updateCar,
                         DeleteCarUseCase deleteCar,
                         GetCarUseCase getCar,
                         ListCarsByOwnerUseCase listCars) {
        this.createCar = createCar;
        this.updateCar = updateCar;
        this.deleteCar = deleteCar;
        this.getCar = getCar;
        this.listCars = listCars;
    }

    @GetMapping
    public CarListResponse list(@CurrentUser AuthenticatedUser user,
                                @RequestParam(required = false) String placa,
                                @RequestParam(required = false) String modelo,
                                @RequestParam(required = false) Integer anio,
                                @RequestParam(required = false) String marca,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size,
                                @RequestParam(required = false) String sort) {
        DomainPage<Car> result = listCars.list(
                user.userId(),
                new CarFilter(placa, modelo, anio, marca),
                new PageRequest(page, size, sort));
        return CarListResponse.from(result);
    }

    @GetMapping("/{id}")
    public CarResponse getById(@CurrentUser AuthenticatedUser user, @PathVariable UUID id) {
        return CarResponse.from(getCar.getByIdForOwner(id, user.userId()));
    }

    @PostMapping
    public ResponseEntity<CarResponse> create(@CurrentUser AuthenticatedUser user,
                                              @Valid @RequestBody CarRequest body) {
        Car created = createCar.create(new CreateCarCommand(
                user.userId(), body.marca(), body.modelo(), body.anio(),
                body.placa(), body.color(), body.fotoUrl()));
        return ResponseEntity.status(HttpStatus.CREATED).body(CarResponse.from(created));
    }

    @PutMapping("/{id}")
    public CarResponse update(@CurrentUser AuthenticatedUser user,
                              @PathVariable UUID id,
                              @Valid @RequestBody CarRequest body) {
        Car updated = updateCar.update(new UpdateCarCommand(
                id, user.userId(), body.marca(), body.modelo(), body.anio(),
                body.placa(), body.color(), body.fotoUrl()));
        return CarResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@CurrentUser AuthenticatedUser user, @PathVariable UUID id) {
        deleteCar.delete(id, user.userId());
        return ResponseEntity.noContent().build();
    }
}
