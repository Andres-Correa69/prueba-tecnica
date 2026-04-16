package develope.cars.domain.vo;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * URL de foto opcional para un carro.
 *
 * <p>Usar {@link Optional} en el dominio señala la regla de negocio
 * "los carros pueden o no tener foto" a nivel de tipo.</p>
 */
public final class FotoUrl {

    private final String value;

    private FotoUrl(String value) { this.value = value; }

    public static Optional<FotoUrl> ofNullable(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        String trimmed = raw.trim();
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException("fotoUrl must be <= 500 chars");
        }
        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("fotoUrl must be http(s)");
            }
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new IllegalArgumentException("fotoUrl is not a valid URI");
        }
        return Optional.of(new FotoUrl(trimmed));
    }

    public String value() { return value; }

    @Override public boolean equals(Object o) { return o instanceof FotoUrl f && Objects.equals(value, f.value); }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
