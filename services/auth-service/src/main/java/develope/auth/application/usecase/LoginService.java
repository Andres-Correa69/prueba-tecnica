package develope.auth.application.usecase;

import develope.auth.application.port.in.AuthResult;
import develope.auth.application.port.in.LoginCommand;
import develope.auth.application.port.in.LoginUseCase;
import develope.auth.application.port.out.PasswordHasherPort;
import develope.auth.application.port.out.TokenIssuerPort;
import develope.auth.application.port.out.UserRepositoryPort;
import develope.auth.domain.exception.InvalidCredentialsException;
import develope.auth.domain.model.User;
import develope.auth.domain.vo.Username;

import java.util.Objects;
import java.util.Optional;

/**
 * Implementación de {@link LoginUseCase}.
 *
 * <p>Nota de seguridad: realizamos la verificación de contraseña incluso cuando el usuario
 * no existe (hasheando un dummy). Esto mantiene el tiempo de respuesta constante
 * independientemente de si el nombre de usuario existe, mitigando la enumeración de cuentas
 * vía canales laterales de tiempo. Sin esto, un chequeo de usuario inexistente retorna en
 * menos de un milisegundo mientras que BCrypt tarda ~70ms — una brecha trivialmente explotable.</p>
 */
public final class LoginService implements LoginUseCase {

    // Precomputado una vez para que el tiempo de verificación sea estable. El valor exacto
    // es irrelevante; lo que importa es que sea un hash BCrypt válido para que el
    // hasher haga el trabajo real en vez de cortocircuitarse.
    private static final String DUMMY_PASSWORD = "not-a-real-password-timing-defense";

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final TokenIssuerPort tokenIssuer;

    public LoginService(UserRepositoryPort users, PasswordHasherPort hasher, TokenIssuerPort tokenIssuer) {
        this.users = Objects.requireNonNull(users);
        this.hasher = Objects.requireNonNull(hasher);
        this.tokenIssuer = Objects.requireNonNull(tokenIssuer);
    }

    @Override
    public AuthResult login(LoginCommand command) {
        Objects.requireNonNull(command, "command");

        Username username;
        try {
            username = Username.of(command.username());
        } catch (IllegalArgumentException badFormat) {
            // Formato inválido → misma respuesta que "credenciales incorrectas". No
            // filtrar "ese usuario es imposible" vs "ese usuario está disponible".
            throw new InvalidCredentialsException();
        }

        Optional<User> maybeUser = users.findByUsername(username);

        // Siempre verificar *algo* para que el tiempo sea constante entre ramas.
        boolean ok;
        if (maybeUser.isEmpty()) {
            // Consumir los mismos ~70ms que tomaría una verificación BCrypt real.
            hasher.hash(DUMMY_PASSWORD);
            ok = false;
        } else {
            ok = hasher.matches(command.rawPassword(), maybeUser.get().passwordHash());
        }

        if (!ok || maybeUser.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        User user = maybeUser.get();
        TokenIssuerPort.IssuedToken issued = tokenIssuer.issue(user);
        return new AuthResult(issued.token(), user.id().value(), user.username().value(), issued.expiresAt());
    }
}
