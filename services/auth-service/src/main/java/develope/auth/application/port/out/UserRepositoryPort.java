package develope.auth.application.port.out;

import develope.auth.domain.model.User;
import develope.auth.domain.vo.Username;

import java.util.Optional;

/**
 * Puerto de salida: abstracción de persistencia para el agregado {@link User}.
 *
 * <p>La capa de aplicación depende ÚNICAMENTE de esta interfaz. Ya sea que la
 * implementación sea JPA+SQL Server hoy, MongoDB mañana, o un fake en memoria
 * en los tests — los casos de uso permanecen iguales. Esta es la mitad de
 * "inversión de dependencias" de la arquitectura hexagonal en la práctica.</p>
 */
public interface UserRepositoryPort {

    boolean existsByUsername(Username username);

    Optional<User> findByUsername(Username username);

    /** Persiste un nuevo usuario o actualiza uno existente (por id). */
    void save(User user);
}
