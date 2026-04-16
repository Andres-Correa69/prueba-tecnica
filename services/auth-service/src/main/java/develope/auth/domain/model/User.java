package develope.auth.domain.model;

import develope.auth.domain.vo.PasswordHash;
import develope.auth.domain.vo.Username;

import java.time.Instant;
import java.util.Objects;

/**
 * Raíz de agregado del bounded context de auth.
 *
 * <p>Inmutable por diseño: cualquier cambio de estado (por ejemplo, rotación de
 * contraseña) debe devolver una <em>nueva</em> instancia. La inmutabilidad hace
 * que razonar sobre el objeto sea trivial — una vez que tienes un {@code User},
 * nada fuera del dominio puede cambiar lo que representa.</p>
 */
public final class User {

    private final UserId id;
    private final Username username;
    private final PasswordHash passwordHash;
    private final Instant createdAt;

    private User(UserId id, Username username, PasswordHash passwordHash, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    /** Factory para usuarios completamente nuevos (id generado, timestamp de creación fijado). */
    public static User register(Username username, PasswordHash hash) {
        return new User(UserId.newId(), username, hash, Instant.now());
    }

    /** Factory usada por el adaptador de persistencia para rehidratar una fila existente. */
    public static User rehydrate(UserId id, Username username, PasswordHash hash, Instant createdAt) {
        return new User(
                Objects.requireNonNull(id),
                Objects.requireNonNull(username),
                Objects.requireNonNull(hash),
                Objects.requireNonNull(createdAt));
    }

    public UserId id() {
        return id;
    }

    public Username username() {
        return username;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
