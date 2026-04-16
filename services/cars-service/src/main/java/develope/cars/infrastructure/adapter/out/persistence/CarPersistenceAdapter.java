package develope.cars.infrastructure.adapter.out.persistence;

import develope.cars.application.port.in.CarFilter;
import develope.cars.application.port.out.CarRepositoryPort;
import develope.cars.application.port.out.DomainPage;
import develope.cars.application.port.out.PageRequest;
import develope.cars.domain.model.Car;
import develope.cars.domain.model.CarId;
import develope.cars.domain.model.OwnerId;
import develope.cars.domain.vo.Placa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementación basada en JPA de {@link CarRepositoryPort}.
 *
 * <p>Usamos {@link Transactional} aquí en lugar de en cada servicio de caso de uso
 * porque:</p>
 * <ul>
 *   <li>Los casos de uso viven en la capa de aplicación, que es libre de Spring por
 *       diseño — por lo que {@code @Transactional} no puede ir ahí.</li>
 *   <li>Cada caso de uso hoy realiza una única escritura de agregado, así que una
 *       transacción por llamada en el adaptador es equivalente en corrección.</li>
 * </ul>
 * <p>Para casos de uso que necesiten <em>atomicidad multi-escritura</em> a futuro (p. ej. una
 * saga), introduciríamos un decorador {@code TransactionalUseCase} en la
 * capa de aplicación.</p>
 */
@Component
public class CarPersistenceAdapter implements CarRepositoryPort {

    private final CarJpaRepository repo;

    public CarPersistenceAdapter(CarJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void save(Car car) {
        // En el update la entidad probablemente existe — se prefiere aplicar los cambios de campo
        // sobre la instancia gestionada (UPDATE generado por Hibernate más limpio).
        repo.findById(car.id().value())
                .map(existing -> CarPersistenceMapper.applyUpdates(existing, car))
                .ifPresentOrElse(
                        repo::save,
                        () -> repo.save(CarPersistenceMapper.toEntity(car)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Car> findByIdAndOwner(CarId id, OwnerId owner) {
        return repo.findByIdAndUserId(id.value(), owner.value())
                .map(CarPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByPlaca(Placa placa) {
        return repo.existsByPlaca(placa.value());
    }

    @Override
    public boolean existsByPlacaAndIdNot(Placa placa, CarId excludedId) {
        return repo.existsByPlacaAndIdNot(placa.value(), excludedId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public DomainPage<Car> findByOwner(OwnerId owner, CarFilter filter, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest spring =
                buildSpringPageable(pageRequest);

        Specification<CarJpaEntity> spec = Specification
                .where(CarSpecifications.ownedBy(owner.value()))
                .and(CarSpecifications.fromFilter(filter));

        Page<CarJpaEntity> page = repo.findAll(spec, spring);

        return new DomainPage<>(
                page.getContent().stream().map(CarPersistenceMapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }

    @Override
    @Transactional
    public void delete(Car car) {
        repo.deleteById(car.id().value());
    }

    /**
     * Parsea la spec de ordenamiento minimalista del puerto ({@code "field,asc"}) a un
     * {@link Sort} de Spring. El ordenamiento por defecto es createdAt DESC para que los
     * carros agregados recientemente aparezcan primero.
     */
    private static org.springframework.data.domain.PageRequest buildSpringPageable(PageRequest req) {
        Sort sort;
        if (req.sort() == null || req.sort().isBlank()) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else {
            String[] parts = req.sort().split(",");
            String prop = parts[0].trim();
            Sort.Direction dir = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(dir, prop);
        }
        return org.springframework.data.domain.PageRequest.of(req.page(), req.size(), sort);
    }
}
