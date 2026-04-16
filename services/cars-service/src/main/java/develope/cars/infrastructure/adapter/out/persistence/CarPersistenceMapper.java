package develope.cars.infrastructure.adapter.out.persistence;

import develope.cars.domain.model.Car;
import develope.cars.domain.model.CarId;
import develope.cars.domain.model.OwnerId;
import develope.cars.domain.vo.Anio;
import develope.cars.domain.vo.Color;
import develope.cars.domain.vo.FotoUrl;
import develope.cars.domain.vo.Marca;
import develope.cars.domain.vo.Modelo;
import develope.cars.domain.vo.Placa;

/** Traductor stateless entre {@link Car} y {@link CarJpaEntity}. */
final class CarPersistenceMapper {

    private CarPersistenceMapper() {}

    static CarJpaEntity toEntity(Car car) {
        return new CarJpaEntity(
                car.id().value(),
                car.ownerId().value(),
                car.marca().value(),
                car.modelo().value(),
                car.anio().value(),
                car.placa().value(),
                car.color().value(),
                car.fotoUrl().map(FotoUrl::value).orElse(null),
                car.createdAt(),
                car.updatedAt()
        );
    }

    /** Usado al actualizar — copia los campos mutables sobre una entidad gestionada existente. */
    static CarJpaEntity applyUpdates(CarJpaEntity target, Car source) {
        target.setMarca(source.marca().value());
        target.setModelo(source.modelo().value());
        target.setAnio(source.anio().value());
        target.setPlaca(source.placa().value());
        target.setColor(source.color().value());
        target.setFotoUrl(source.fotoUrl().map(FotoUrl::value).orElse(null));
        target.setUpdatedAt(source.updatedAt());
        return target;
    }

    static Car toDomain(CarJpaEntity e) {
        return Car.rehydrate(
                CarId.of(e.getId()),
                OwnerId.of(e.getUserId()),
                Marca.of(e.getMarca()),
                Modelo.of(e.getModelo()),
                Anio.of(e.getAnio()),
                Placa.of(e.getPlaca()),
                Color.of(e.getColor()),
                FotoUrl.ofNullable(e.getFotoUrl()),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
