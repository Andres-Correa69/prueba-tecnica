package develope.cars.domain.vo;

import java.time.Year;
import java.util.Objects;

/**
 * Value object del año de fabricación.
 *
 * <p>Regla: entre 1900 y <em>año actual + 1</em>. El "+1" permite
 * registrar un modelo 2027 en diciembre de 2026.</p>
 *
 * <p>La regla exacta se centraliza aquí — si el negocio luego dice
 * "solo hasta el año actual", se cambia este único método.</p>
 */
public final class Anio {

    public static final int MIN_YEAR = 1900;

    private final int value;

    private Anio(int value) { this.value = value; }

    public static Anio of(int value) {
        int maxAllowed = Year.now().getValue() + 1;
        if (value < MIN_YEAR || value > maxAllowed) {
            throw new IllegalArgumentException(
                    "año must be between " + MIN_YEAR + " and " + maxAllowed);
        }
        return new Anio(value);
    }

    public int value() { return value; }

    @Override public boolean equals(Object o) { return o instanceof Anio a && value == a.value; }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return Integer.toString(value); }
}
