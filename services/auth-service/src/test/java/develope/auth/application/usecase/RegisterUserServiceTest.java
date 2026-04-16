package develope.auth.application.usecase;

import develope.auth.application.port.in.RegisterUserCommand;
import develope.auth.application.port.out.PasswordHasherPort;
import develope.auth.application.port.out.UserRepositoryPort;
import develope.auth.domain.exception.UsernameAlreadyExistsException;
import develope.auth.domain.model.User;
import develope.auth.domain.vo.PasswordHash;
import develope.auth.domain.vo.Username;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitarios para {@link RegisterUserService}. Sin Spring — cableamos los puertos
 * a mano usando fakes simples en memoria. Este es uno de los beneficios prácticos
 * de la arquitectura hexagonal: tests rápidos, deterministas y libres del framework.
 */
class RegisterUserServiceTest {

    @Test
    void registers_a_new_user_with_hashed_password() {
        InMemoryUsers users = new InMemoryUsers();
        FakeHasher hasher = new FakeHasher();

        RegisterUserService service = new RegisterUserService(users, hasher);
        var id = service.register(new RegisterUserCommand("andres", "Test1234!"));

        assertNotNull(id);
        assertTrue(users.existsByUsername(Username.of("andres")));
        // El hash almacenado NO es la contraseña en bruto.
        assertTrue(users.store.values().stream()
                .noneMatch(u -> u.passwordHash().value().equals("Test1234!")));
    }

    @Test
    void rejects_duplicate_username() {
        InMemoryUsers users = new InMemoryUsers();
        FakeHasher hasher = new FakeHasher();
        RegisterUserService service = new RegisterUserService(users, hasher);

        service.register(new RegisterUserCommand("andres", "Test1234!"));
        assertThrows(UsernameAlreadyExistsException.class,
                () -> service.register(new RegisterUserCommand("andres", "Other1234")));
    }

    @Test
    void rejects_weak_password() {
        InMemoryUsers users = new InMemoryUsers();
        FakeHasher hasher = new FakeHasher();
        RegisterUserService service = new RegisterUserService(users, hasher);

        assertThrows(IllegalArgumentException.class,
                () -> service.register(new RegisterUserCommand("andres", "short")));
    }

    // --- fakes --------------------------------------------------------
    // (dobles de prueba en memoria para los puertos)

    static class InMemoryUsers implements UserRepositoryPort {
        final Map<String, User> store = new HashMap<>();
        @Override public boolean existsByUsername(Username u) { return store.containsKey(u.value()); }
        @Override public Optional<User> findByUsername(Username u) { return Optional.ofNullable(store.get(u.value())); }
        @Override public void save(User user) { store.put(user.username().value(), user); }
    }

    static class FakeHasher implements PasswordHasherPort {
        @Override public PasswordHash hash(String raw) { return PasswordHash.of("HASHED::" + raw); }
        @Override public boolean matches(String raw, PasswordHash h) { return h.value().equals("HASHED::" + raw); }
    }
}
