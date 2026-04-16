package develope.auth.application.port.in;

/**
 * Comando de entrada para el caso de uso de login.
 *
 * @param username   nombre de usuario en bruto
 * @param rawPassword contraseña en texto plano provista por el llamador; se compara contra
 *                    el hash BCrypt almacenado usando el {@link develope.auth.application.port.out.PasswordHasherPort}.
 */
public record LoginCommand(String username, String rawPassword) {}
