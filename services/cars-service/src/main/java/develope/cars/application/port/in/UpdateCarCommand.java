package develope.cars.application.port.in;

import java.util.UUID;

/**
 * Comando de entrada para actualizar un carro.
 *
 * <p>Misma regla de seguridad que {@link CreateCarCommand}: {@code ownerId} es
 * el id del usuario autenticado extraído del JWT, no un campo de la petición.</p>
 */
public record UpdateCarCommand(
        UUID carId,
        UUID ownerId,
        String marca,
        String modelo,
        int anio,
        String placa,
        String color,
        String fotoUrl
) {}
