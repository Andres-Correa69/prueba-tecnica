package develope.cars.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identificador fuertemente tipado para un Car. Ver {@code UserId} en auth-service para la justificación. */
public final class CarId {

    private final UUID value;

    private CarId(UUID value) { this.value = value; }

    public static CarId of(UUID value) {
        return new CarId(Objects.requireNonNull(value, "CarId value is required"));
    }

    public static CarId newId() { return new CarId(UUID.randomUUID()); }

    public UUID value() { return value; }

    @Override public boolean equals(Object o) { return o instanceof CarId c && Objects.equals(value, c.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value.toString(); }
}
