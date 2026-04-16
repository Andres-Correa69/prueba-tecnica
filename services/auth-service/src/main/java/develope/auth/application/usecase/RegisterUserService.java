package develope.auth.application.usecase;

import develope.auth.application.port.in.RegisterUserCommand;
import develope.auth.application.port.in.RegisterUserUseCase;
import develope.auth.application.port.out.PasswordHasherPort;
import develope.auth.application.port.out.UserRepositoryPort;
import develope.auth.domain.exception.UsernameAlreadyExistsException;
import develope.auth.domain.model.User;
import develope.auth.domain.vo.PasswordHash;
import develope.auth.domain.vo.Username;

import java.util.Objects;
import java.util.UUID;

/**
 * Implementación de {@link RegisterUserUseCase}.
 *
 * <p>Nótese que esta clase <em>intencionalmente</em> no está anotada con
 * {@code @Service}. Eso la mantiene agnóstica a Spring: compila en un módulo
 * que ni siquiera tiene spring-context en el classpath, y puede
 * probarse unitariamente con JUnit puro + cableado manual por constructor.</p>
 *
 * <p>El cableado de Spring ocurre una sola vez, centralmente, en {@code BeanConfig}. Ese
 * "cableado manual" es más largo que esparcir anotaciones, pero hace que el
 * grafo de dependencias sea completamente explícito — puedes leer BeanConfig como
 * un mapa de la aplicación.</p>
 */
public final class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;

    public RegisterUserService(UserRepositoryPort users, PasswordHasherPort hasher) {
        this.users = Objects.requireNonNull(users);
        this.hasher = Objects.requireNonNull(hasher);
    }

    @Override
    public UUID register(RegisterUserCommand command) {
        Objects.requireNonNull(command, "command");

        // 1. Construir el Value Object — aquí es donde corre la validación del dominio.
        //    Si `username` está mal formado, fallamos rápido con IllegalArgumentException
        //    que GlobalExceptionHandler traduce a HTTP 400.
        Username username = Username.of(command.username());

        // 2. La política mínima de contraseña vive aquí (no en el VO, porque el VO
        //    solo almacena el hash — la contraseña plana nunca entra al dominio).
        validateRawPassword(command.rawPassword());

        // 3. Aplicar unicidad *antes* de hashear (hashear es costoso).
        if (users.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username.value());
        }

        // 4. Hashear + persistir. Nótese el VO PasswordHash — carga la
        //    invariante "hasheada", así que el agregado User no puede construirse
        //    con una contraseña plana por accidente.
        PasswordHash hash = hasher.hash(command.rawPassword());
        User user = User.register(username, hash);
        users.save(user);

        return user.id().value();
    }

    /**
     * Reglas de contraseña (intencionalmente modestas para una demo):
     * - De 8 a 100 caracteres.
     * - Debe contener al menos una letra Y un dígito.
     * Estas pueden endurecerse después sin tocar las capas REST o de persistencia
     * — viven por completo en la capa de aplicación.
     */
    private static void validateRawPassword(String raw) {
        if (raw == null || raw.length() < 8 || raw.length() > 100) {
            throw new IllegalArgumentException("password must be 8-100 chars");
        }
        boolean hasLetter = raw.chars().anyMatch(Character::isLetter);
        boolean hasDigit = raw.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("password must contain letters and digits");
        }
    }
}
