package develope.cars.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de Spring Data con consultas derivadas simples Y
 * {@link JpaSpecificationExecutor} para filtrado dinámico.
 *
 * <p>Las Specifications (ver {@code CarSpecifications}) nos permiten construir
 * predicados null-safe y componibles en tiempo de ejecución — que es justo lo que
 * necesita el endpoint de búsqueda.</p>
 */
public interface CarJpaRepository extends JpaRepository<CarJpaEntity, UUID>, JpaSpecificationExecutor<CarJpaEntity> {

    Optional<CarJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, UUID excludedId);
}
