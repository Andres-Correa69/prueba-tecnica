package develope.cars.application.port.in;

import java.util.UUID;

/**
 * Comando de entrada para crear un carro.
 *
 * <p><strong>Crítico para seguridad</strong>: {@code ownerId} se toma del
 * claim {@code sub} del JWT en el controlador — NUNCA del cuerpo de la
 * petición. Un cliente malicioso que envíe {@code "ownerId": "someone-else"} en
 * el JSON simplemente vería ese campo ignorado.</p>
 */
public record CreateCarCommand(
        UUID ownerId,
        String marca,
        String modelo,
        int anio,
        String placa,
        String color,
        String fotoUrl
) {}
