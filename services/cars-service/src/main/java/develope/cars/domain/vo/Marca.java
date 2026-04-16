package develope.cars.domain.vo;

import java.util.Objects;

public final class Marca {

    private final String value;

    private Marca(String value) { this.value = value; }

    public static Marca of(String raw) {
        if (raw == null) throw new IllegalArgumentException("marca is required");
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || trimmed.length() > 50) {
            throw new IllegalArgumentException("marca must be 1-50 chars");
        }
        return new Marca(trimmed);
    }

    public String value() { return value; }

    @Override public boolean equals(Object o) { return o instanceof Marca m && Objects.equals(value, m.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
