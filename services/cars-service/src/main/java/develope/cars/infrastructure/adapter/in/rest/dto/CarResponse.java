package develope.cars.infrastructure.adapter.in.rest.dto;

import develope.cars.domain.model.Car;

import java.time.Instant;
import java.util.UUID;

public record CarResponse(
        UUID id,
        UUID ownerId,
        String marca,
        String modelo,
        int anio,
        String placa,
        String color,
        String fotoUrl,
        Instant createdAt,
        Instant updatedAt
) {
    public static CarResponse from(Car car) {
        return new CarResponse(
                car.id().value(),
                car.ownerId().value(),
                car.marca().value(),
                car.modelo().value(),
                car.anio().value(),
                car.placa().value(),
                car.color().value(),
                car.fotoUrl().map(f -> f.value()).orElse(null),
                car.createdAt(),
                car.updatedAt()
        );
    }
}
