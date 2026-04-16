package develope.auth.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA — un espejo mecánico 1 a 1 de la tabla {@code auth.Users}.
 *
 * <p>Deliberadamente <em>no</em> es el mismo tipo que el {@link develope.auth.domain.model.User} del dominio:
 * de lo contrario las anotaciones JPA contaminarían el dominio con una dependencia
 * del framework, y los dos podrían necesitar divergir (por ejemplo, una columna de BD que
 * no queremos exponer al dominio).</p>
 *
 * <p>El mapeo desde/hacia el dominio vive en {@link UserPersistenceMapper}.</p>
 *
 * <p>Se exponen métodos setter (no records) porque JPA requiere un
 * constructor sin argumentos y campos escribibles; esas son las ÚNICAS dos
 * restricciones JPA que toleramos, y permanecen confinadas a esta clase.</p>
 */
@Entity
@Table(name = "Users", schema = "auth")
public class UserJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 30)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UserJpaEntity() {
        // requerido por JPA
    }

    public UserJpaEntity(UUID id, String username, String passwordHash, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }

    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
