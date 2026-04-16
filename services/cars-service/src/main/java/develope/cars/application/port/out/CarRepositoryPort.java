package develope.cars.application.port.out;

import develope.cars.application.port.in.CarFilter;
import develope.cars.domain.model.Car;
import develope.cars.domain.model.CarId;
import develope.cars.domain.model.OwnerId;
import develope.cars.domain.vo.Placa;

import java.util.Optional;

/**
 * Puerto de salida para la persistencia de {@link Car}.
 *
 * <p>Cada método de mutación recibe un {@link OwnerId}: esta es la piedra angular
 * de nuestro control de acceso. El findByIdAndOwner del repositorio garantiza que
 * los usuarios solo puedan ver sus propios carros — omitir esa protección sería una
 * vulnerabilidad IDOR.</p>
 */
public interface CarRepositoryPort {

    void save(Car car);

    Optional<Car> findByIdAndOwner(CarId id, OwnerId owner);

    boolean existsByPlaca(Placa placa);

    /** Usado al actualizar un carro: "¿esta placa ya está usada por OTRA persona?". */
    boolean existsByPlacaAndIdNot(Placa placa, CarId excludedId);

    DomainPage<Car> findByOwner(OwnerId owner, CarFilter filter, PageRequest pageRequest);

    void delete(Car car);
}
