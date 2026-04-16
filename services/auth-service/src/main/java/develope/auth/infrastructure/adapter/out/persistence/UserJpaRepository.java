package develope.auth.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA.
 *
 * <p>Esta interfaz es específica de Spring; está bien — vive estrictamente dentro
 * del adaptador de persistencia. La capa de aplicación nunca la ve.</p>
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
