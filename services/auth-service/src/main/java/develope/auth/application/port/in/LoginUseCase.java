package develope.auth.application.port.in;

/**
 * Puerto de entrada: autentica a un usuario y devuelve un JWT firmado.
 */
public interface LoginUseCase {

    /**
     * @throws develope.auth.domain.exception.InvalidCredentialsException
     *         cuando el usuario no existe o la contraseña no coincide
     *         con el hash almacenado. Intencionalmente no revela
     *         cuál fue — previene la enumeración de cuentas.
     */
    AuthResult login(LoginCommand command);
}
