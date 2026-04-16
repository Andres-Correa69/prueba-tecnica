package develope.cars.domain.model;

import develope.cars.domain.exception.CarNotOwnedByUserException;
import develope.cars.domain.vo.Anio;
import develope.cars.domain.vo.Color;
import develope.cars.domain.vo.FotoUrl;
import develope.cars.domain.vo.Marca;
import develope.cars.domain.vo.Modelo;
import develope.cars.domain.vo.Placa;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Raíz de agregado para un carro.
 *
 * <p>Inmutable: las actualizaciones devuelven nuevas instancias vía {@link #withUpdates}.
 * Las verificaciones de propiedad son una preocupación de primer nivel del dominio: ver
 * {@link #ensureOwnedBy(OwnerId)}, usado por los casos de uso de actualización / borrado.</p>
 */
public final class Car {

    private final CarId id;
    private final OwnerId ownerId;
    private final Marca marca;
    private final Modelo modelo;
    private final Anio anio;
    private final Placa placa;
    private final Color color;
    private final FotoUrl fotoUrl; // nullable → Optional en el accesor
    private final Instant createdAt;
    private final Instant updatedAt;

    private Car(CarId id, OwnerId ownerId, Marca marca, Modelo modelo, Anio anio,
                Placa placa, Color color, FotoUrl fotoUrl, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.placa = placa;
        this.color = color;
        this.fotoUrl = fotoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Factory para carros recién creados (nuevo id + timestamps). */
    public static Car create(OwnerId ownerId, Marca marca, Modelo modelo, Anio anio,
                             Placa placa, Color color, Optional<FotoUrl> fotoUrl) {
        Instant now = Instant.now();
        return new Car(CarId.newId(), ownerId, marca, modelo, anio, placa, color,
                fotoUrl.orElse(null), now, now);
    }

    /** Factory usada por el adaptador de persistencia para rehidratar desde la BD. */
    public static Car rehydrate(CarId id, OwnerId ownerId, Marca marca, Modelo modelo, Anio anio,
                                Placa placa, Color color, Optional<FotoUrl> fotoUrl,
                                Instant createdAt, Instant updatedAt) {
        return new Car(
                Objects.requireNonNull(id), Objects.requireNonNull(ownerId),
                Objects.requireNonNull(marca), Objects.requireNonNull(modelo),
                Objects.requireNonNull(anio), Objects.requireNonNull(placa),
                Objects.requireNonNull(color),
                fotoUrl.orElse(null),
                Objects.requireNonNull(createdAt), Objects.requireNonNull(updatedAt));
    }

    /**
     * Devuelve un nuevo Car con los campos mutables actualizados (todo excepto id
     * y ownerId). updatedAt se refresca.
     */
    public Car withUpdates(Marca marca, Modelo modelo, Anio anio, Placa placa, Color color,
                           Optional<FotoUrl> fotoUrl) {
        return new Car(id, ownerId, marca, modelo, anio, placa, color,
                fotoUrl.orElse(null), createdAt, Instant.now());
    }

    /**
     * Guarda central de propiedad. Cada caso de uso de mutación llama a esto ANTES
     * de tocar la BD — previene IDOR (Insecure Direct Object Reference / referencia
     * directa insegura a objetos).
     */
    public void ensureOwnedBy(OwnerId candidate) {
        if (!ownerId.equals(candidate)) {
            throw new CarNotOwnedByUserException();
        }
    }

    public CarId id()           { return id; }
    public OwnerId ownerId()    { return ownerId; }
    public Marca marca()        { return marca; }
    public Modelo modelo()      { return modelo; }
    public Anio anio()          { return anio; }
    public Placa placa()        { return placa; }
    public Color color()        { return color; }
    public Optional<FotoUrl> fotoUrl() { return Optional.ofNullable(fotoUrl); }
    public Instant createdAt()  { return createdAt; }
    public Instant updatedAt()  { return updatedAt; }
}
