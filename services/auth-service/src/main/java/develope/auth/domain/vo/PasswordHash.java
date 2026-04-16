package develope.auth.domain.vo;

import java.util.Objects;

/**
 * Value Object que envuelve una contraseña ya hasheada.
 *
 * <p>El dominio nunca almacena ni acepta una contraseña en texto plano; la única
 * forma de obtener un {@link PasswordHash} es a través de una implementación de
 * {@link develope.auth.application.port.out.PasswordHasherPort} (actualmente BCrypt).
 * Eso garantiza que podemos buscar en la base de código "contraseña plana en el
 * dominio" y no encontrar ninguna coincidencia.</p>
 */
public final class PasswordHash {

    private final String value;

    private PasswordHash(String value) {
        this.value = value;
    }

    public static PasswordHash of(String alreadyHashed) {
        if (alreadyHashed == null || alreadyHashed.isBlank()) {
            throw new IllegalArgumentException("password hash cannot be blank");
        }
        return new PasswordHash(alreadyHashed);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PasswordHash p && Objects.equals(value, p.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    /**
     * Intencionalmente no devuelve el valor del hash — evita el registro
     * accidental de hashes de contraseñas en logs.
     */
    @Override
    public String toString() {
        return "PasswordHash{***}";
    }
}
