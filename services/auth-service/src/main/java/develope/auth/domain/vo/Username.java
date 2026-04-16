package develope.auth.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object que garantiza las invariantes de un nombre de usuario válido.
 *
 * <p>Por qué un Value Object en vez de un {@link String} plano:</p>
 * <ul>
 *   <li>Imposible construir uno inválido — la validación se ejecuta en el
 *       constructor, así que cualquier método que acepte un {@code Username}
 *       tiene garantizado un valor correcto (se acabó el "¿dónde valido esto?").</li>
 *   <li>Seguridad de tipos: los métodos ya no reciben dos Strings intercambiables.</li>
 *   <li>Todas las reglas de negocio para nombres de usuario viven aquí, en UN solo lugar.</li>
 * </ul>
 *
 * <p>Reglas:
 * <ul>
 *   <li>De 3 a 30 caracteres.</li>
 *   <li>Solo letras ASCII, dígitos, punto, guion bajo y guion.</li>
 *   <li>Se almacena en minúsculas para que "Andres" y "andres" no puedan registrarse a la vez.</li>
 * </ul>
 */
public final class Username {

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,30}$");

    private final String value;

    private Username(String value) {
        this.value = value;
    }

    /**
     * Factory + validación. Lanza {@link IllegalArgumentException} para mantener el
     * dominio libre de Spring/Jakarta; los adaptadores REST traducen eso a HTTP 400.
     */
    public static Username of(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("username is required");
        }
        String trimmed = raw.trim();
        if (!PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "username must be 3-30 chars (letters, digits, . _ -)");
        }
        return new Username(trimmed.toLowerCase());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Username u && Objects.equals(value, u.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
