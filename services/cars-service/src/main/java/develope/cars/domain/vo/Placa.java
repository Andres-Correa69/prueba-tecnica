package develope.cars.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object de la placa vehicular.
 *
 * <p>El regex es intencionalmente amplio: 3 letras + 3 dígitos (p. ej. ABC123) o
 * 3 letras + guion + 3 dígitos (p. ej. ABC-123), además de la variante con
 * 3 dígitos + 3 letras usada en algunos países latinoamericanos. Ajústalo
 * a tu jurisdicción si hace falta; la validación vive en UN solo lugar.</p>
 */
public final class Placa {

    private static final Pattern PATTERN = Pattern.compile("^[A-Z0-9]{3}-?[A-Z0-9]{3}$");

    private final String value;

    private Placa(String value) { this.value = value; }

    public static Placa of(String raw) {
        if (raw == null) throw new IllegalArgumentException("placa is required");
        String normalized = raw.trim().toUpperCase().replace(" ", "");
        if (!PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "placa must match letters/digits pattern, got: " + raw);
        }
        return new Placa(normalized);
    }

    public String value() { return value; }

    @Override public boolean equals(Object o) { return o instanceof Placa p && Objects.equals(value, p.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
