package develope.cars.domain.vo;

import java.util.Objects;

public final class Color {

    private final String value;

    private Color(String value) { this.value = value; }

    public static Color of(String raw) {
        if (raw == null) throw new IllegalArgumentException("color is required");
        String trimmed = raw.trim();
        if (trimmed.isEmpty() || trimmed.length() > 30) {
            throw new IllegalArgumentException("color must be 1-30 chars");
        }
        return new Color(trimmed);
    }

    public String value() { return value; }

    @Override public boolean equals(Object o) { return o instanceof Color c && Objects.equals(value, c.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
