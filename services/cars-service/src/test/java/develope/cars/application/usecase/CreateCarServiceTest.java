package develope.cars.application.usecase;

import develope.cars.application.port.in.CarFilter;
import develope.cars.application.port.in.CreateCarCommand;
import develope.cars.application.port.out.CarRepositoryPort;
import develope.cars.application.port.out.DomainPage;
import develope.cars.application.port.out.PageRequest;
import develope.cars.domain.exception.PlacaAlreadyExistsException;
import develope.cars.domain.model.Car;
import develope.cars.domain.model.CarId;
import develope.cars.domain.model.OwnerId;
import develope.cars.domain.vo.Placa;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CreateCarServiceTest {

    @Test
    void creates_car_scoped_to_owner() {
        InMemoryCars cars = new InMemoryCars();
        CreateCarService svc = new CreateCarService(cars);
        UUID owner = UUID.randomUUID();

        Car created = svc.create(new CreateCarCommand(
                owner, "Toyota", "Corolla", 2020, "ABC123", "Rojo", null));

        assertEquals(owner, created.ownerId().value());
        assertEquals("ABC123", created.placa().value());
        assertTrue(cars.findByIdAndOwner(created.id(), OwnerId.of(owner)).isPresent());
    }

    @Test
    void rejects_duplicate_placa() {
        InMemoryCars cars = new InMemoryCars();
        CreateCarService svc = new CreateCarService(cars);
        UUID owner = UUID.randomUUID();

        svc.create(new CreateCarCommand(owner, "Toyota", "Corolla", 2020, "ABC123", "Rojo", null));
        assertThrows(PlacaAlreadyExistsException.class, () ->
                svc.create(new CreateCarCommand(owner, "Mazda", "3", 2021, "ABC123", "Azul", null)));
    }

    static class InMemoryCars implements CarRepositoryPort {
        final Map<UUID, Car> byId = new HashMap<>();
        final Set<String> placas = new HashSet<>();

        @Override public void save(Car car) { byId.put(car.id().value(), car); placas.add(car.placa().value()); }
        @Override public Optional<Car> findByIdAndOwner(CarId id, OwnerId owner) {
            Car c = byId.get(id.value());
            return c != null && c.ownerId().equals(owner) ? Optional.of(c) : Optional.empty();
        }
        @Override public boolean existsByPlaca(Placa placa) { return placas.contains(placa.value()); }
        @Override public boolean existsByPlacaAndIdNot(Placa placa, CarId excludedId) {
            return byId.values().stream()
                    .anyMatch(c -> c.placa().equals(placa) && !c.id().equals(excludedId));
        }
        @Override public DomainPage<Car> findByOwner(OwnerId owner, CarFilter f, PageRequest p) {
            List<Car> list = byId.values().stream()
                    .filter(c -> c.ownerId().equals(owner))
                    .toList();
            return new DomainPage<>(list, p.page(), p.size(), list.size());
        }
        @Override public void delete(Car car) {
            byId.remove(car.id().value());
            placas.remove(car.placa().value());
        }
    }
}
