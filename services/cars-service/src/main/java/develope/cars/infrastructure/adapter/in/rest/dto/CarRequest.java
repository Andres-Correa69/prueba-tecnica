package develope.cars.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la petición tanto para POST /cars como para PUT /cars/{id}.
 *
 * <p>Nótese que NO hay campo {@code ownerId}. El dueño se resuelve en el servidor
 * a partir del JWT — ver {@code CurrentUserArgumentResolver}.</p>
 */
public record CarRequest(
        @NotBlank @Size(max = 50) String marca,
        @NotBlank @Size(max = 50) String modelo,
        @Min(1900) @Max(2100)     int anio,
        @NotBlank @Size(max = 10) String placa,
        @NotBlank @Size(max = 30) String color,
        @Size(max = 500)          String fotoUrl
) {}
