package develope.cars.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA que refleja {@code cars.Cars}.
 *
 * <p>Nótese que {@code user_id} es una columna {@code UUID} simple — no hay
 * {@code @ManyToOne} a una entidad User. Razón: el dueño vive en un
 * schema distinto, propiedad de otro microservicio. Las llaves foráneas
 * entre servicios son un anti-patrón en una arquitectura de microservicios porque
 * acoplan las bases de datos y requieren transacciones entre servicios. Imponemos
 * que "el id del dueño debe corresponder a un usuario real" en la frontera de confianza:
 * el JWT que transportó ese id fue firmado por auth-service, punto.</p>
 */
@Entity
@Table(name = "Cars", schema = "cars")
public class CarJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 50)
    private String modelo;

    @Column(name = "anio", nullable = false)
    private int anio;

    @Column(name = "placa", nullable = false, unique = true, length = 10)
    private String placa;

    @Column(name = "color", nullable = false, length = 30)
    private String color;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CarJpaEntity() { }

    public CarJpaEntity(UUID id, UUID userId, String marca, String modelo, int anio, String placa,
                        String color, String fotoUrl, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.placa = placa;
        this.color = color;
        this.fotoUrl = fotoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public int getAnio() { return anio; }
    public String getPlaca() { return placa; }
    public String getColor() { return color; }
    public String getFotoUrl() { return fotoUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setMarca(String marca) { this.marca = marca; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public void setAnio(int anio) { this.anio = anio; }
    public void setPlaca(String placa) { this.placa = placa; }
    public void setColor(String color) { this.color = color; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
