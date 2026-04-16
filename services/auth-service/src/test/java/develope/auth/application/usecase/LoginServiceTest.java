package develope.auth.application.usecase;

import develope.auth.application.port.in.AuthResult;
import develope.auth.application.port.in.LoginCommand;
import develope.auth.application.port.out.PasswordHasherPort;
import develope.auth.application.port.out.TokenIssuerPort;
import develope.auth.application.port.out.UserRepositoryPort;
import develope.auth.domain.exception.InvalidCredentialsException;
import develope.auth.domain.model.User;
import develope.auth.domain.vo.PasswordHash;
import develope.auth.domain.vo.Username;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {

    @Test
    void issues_token_for_valid_credentials() {
        var users = new InMemoryUsers();
        var hasher = new FakeHasher();
        var tokenIssuer = new FakeTokenIssuer();

        users.save(User.register(Username.of("andres"), hasher.hash("Test1234!")));

        LoginService svc = new LoginService(users, hasher, tokenIssuer);
        AuthResult result = svc.login(new LoginCommand("andres", "Test1234!"));
        assertEquals("andres", result.username());
        assertNotNull(result.token());
    }

    @Test
    void rejects_bad_password_without_leaking_which_failed() {
        var users = new InMemoryUsers();
        var hasher = new FakeHasher();
        var tokenIssuer = new FakeTokenIssuer();
        users.save(User.register(Username.of("andres"), hasher.hash("Test1234!")));

        LoginService svc = new LoginService(users, hasher, tokenIssuer);
        assertThrows(InvalidCredentialsException.class,
                () -> svc.login(new LoginCommand("andres", "wrong")));
    }

    @Test
    void rejects_missing_user_with_same_error() {
        var svc = new LoginService(new InMemoryUsers(), new FakeHasher(), new FakeTokenIssuer());
        assertThrows(InvalidCredentialsException.class,
                () -> svc.login(new LoginCommand("ghost", "whatever123")));
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
        @Override public PasswordHash hash(String raw) { return PasswordHash.of("H::" + raw); }
        @Override public boolean matches(String raw, PasswordHash h) { return h.value().equals("H::" + raw); }
    }
    static class FakeTokenIssuer implements TokenIssuerPort {
        @Override public IssuedToken issue(User user) { return new IssuedToken("token-for-" + user.username(), Instant.now().plusSeconds(60)); }
    }
}
