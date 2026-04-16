package develope.auth.application.port.out;

import develope.auth.domain.model.User;

import java.time.Instant;

/**
 * Puerto de salida: emite un token de acceso para un {@link User} dado.
 *
 * <p>Hoy el adaptador emite JWT HS256. Si algún día migramos a RS256
 * (asimétrico, con JWKS) o a tokens opacos almacenados en Redis, solo el
 * adaptador cambia — los casos de uso siguen llamando a {@link #issue(User)} y
 * reciben un {@link IssuedToken}.</p>
 */
public interface TokenIssuerPort {

    IssuedToken issue(User user);

    /** Pequeño contenedor de datos para que el caso de uso pueda devolver tanto el token como la expiración al llamador. */
    record IssuedToken(String token, Instant expiresAt) {}
}
