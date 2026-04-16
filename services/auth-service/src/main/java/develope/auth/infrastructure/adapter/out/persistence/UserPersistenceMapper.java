package develope.auth.infrastructure.adapter.out.persistence;

import develope.auth.domain.model.User;
import develope.auth.domain.model.UserId;
import develope.auth.domain.vo.PasswordHash;
import develope.auth.domain.vo.Username;

/**
 * Traductor stateless entre el agregado de dominio y la entidad JPA.
 *
 * <p>Vive del lado del adaptador — nunca dentro del dominio. Tenerlo en una
 * clase dedicada significa que puedes leer `User.java` sin pensar jamás
 * en los nombres de columna de JPA, y puedes correr migraciones JPA sin preocuparte
 * por si el dominio compiló.</p>
 */
final class UserPersistenceMapper {

    private UserPersistenceMapper() {}

    static UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.id().value(),
                user.username().value(),
                user.passwordHash().value(),
                user.createdAt());
    }

    static User toDomain(UserJpaEntity entity) {
        return User.rehydrate(
                UserId.of(entity.getId()),
                Username.of(entity.getUsername()),
                PasswordHash.of(entity.getPasswordHash()),
                entity.getCreatedAt());
    }
}
