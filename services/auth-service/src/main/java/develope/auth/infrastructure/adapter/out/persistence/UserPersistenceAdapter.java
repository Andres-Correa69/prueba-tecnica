package develope.auth.infrastructure.adapter.out.persistence;

import develope.auth.application.port.out.UserRepositoryPort;
import develope.auth.domain.model.User;
import develope.auth.domain.vo.Username;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador que implementa {@link UserRepositoryPort} respaldado por JPA.
 *
 * <p>¿Por qué una clase separada en vez de dejar que la interfaz de Spring Data
 * implemente <em>directamente</em> el puerto? Porque el puerto pertenece al
 * lenguaje del dominio ({@code Username}, {@code User}), mientras que Spring Data
 * devuelve {@code UserJpaEntity}. Traducimos en el límite — el
 * dominio nunca ve un tipo JPA.</p>
 */
@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository repo;

    public UserPersistenceAdapter(UserJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean existsByUsername(Username username) {
        return repo.existsByUsername(username.value());
    }

    @Override
    public Optional<User> findByUsername(Username username) {
        return repo.findByUsername(username.value()).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public void save(User user) {
        repo.save(UserPersistenceMapper.toEntity(user));
    }
}
