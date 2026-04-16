package develope.cars.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador del usuario dueño de un carro.
 *
 * <p>El valor refleja {@code auth.Users.id}. cars-service nunca consulta
 * la tabla auth — confía en que el claim {@code sub} del JWT es el
 * id del dueño.</p>
 */
public final class OwnerId {

    private final UUID value;

    private OwnerId(UUID value) { this.value = value; }

    public static OwnerId of(UUID value) {
        return new OwnerId(Objects.requireNonNull(value, "OwnerId value is required"));
    }

    public UUID value() { return value; }

    @Override public boolean equals(Object o) { return o instanceof OwnerId c && Objects.equals(value, c.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value.toString(); }
}
