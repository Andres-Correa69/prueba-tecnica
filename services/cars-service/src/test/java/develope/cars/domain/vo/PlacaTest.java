package develope.cars.domain.vo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlacaTest {

    @Test
    void accepts_letters_digits_no_hyphen() {
        assertEquals("ABC123", Placa.of("abc123").value());
    }

    @Test
    void accepts_letters_digits_with_hyphen() {
        assertEquals("ABC-123", Placa.of("abc-123").value());
    }

    @Test
    void rejects_too_short() {
        assertThrows(IllegalArgumentException.class, () -> Placa.of("AB1"));
    }

    @Test
    void rejects_non_alphanumeric() {
        assertThrows(IllegalArgumentException.class, () -> Placa.of("ABC#23"));
    }

    @Test
    void rejects_null() {
        assertThrows(IllegalArgumentException.class, () -> Placa.of(null));
    }
}
