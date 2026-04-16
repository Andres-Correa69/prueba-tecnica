package develope.auth.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador fuertemente tipado para un User.
 *
 * <p>Usar un tipo dedicado (y no un {@link UUID} pelado) previene la mezcla
 * accidental con otros IDs (por ejemplo {@code CarId}) en tiempo de compilación —
 * una salvaguarda muy barata que rinde frutos cada vez que refactorizamos.</p>
 */
public final class UserId {

    private final UUID value;

    private UserId(UUID value) {
        this.value = value;
    }

    public static UserId of(UUID value) {
        return new UserId(Objects.requireNonNull(value, "UserId value is required"));
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserId id && Objects.equals(value, id.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
