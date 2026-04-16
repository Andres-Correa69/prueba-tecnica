package develope.cars.domain.vo;

import java.util.Objects;

public final class Modelo {

    private final String value;

    private Modelo(String value) { this.value = value; }

    public static Modelo of(String raw) {
        if (raw == null) throw new IllegalArgumentException("modelo is required");
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || trimmed.length() > 50) {
            throw new IllegalArgumentException("modelo must be 1-50 chars");
        }
        return new Modelo(trimmed);
    }

    public String value() { return value; }

    @Override public boolean equals(Object o) { return o instanceof Modelo m && Objects.equals(value, m.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
