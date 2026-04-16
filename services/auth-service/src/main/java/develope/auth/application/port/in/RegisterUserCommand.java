package develope.auth.application.port.in;

/**
 * Comando de entrada para el caso de uso de registro.
 *
 * <p>Los records hacen los comandos inmutables y autodescriptivos. Además obligan
 * al llamador (adaptador REST, tests, etc.) a proveer cada campo explícitamente —
 * sin ambigüedad de setters.</p>
 *
 * @param username nombre de usuario en bruto (validado por el Value Object del dominio)
 * @param rawPassword contraseña en texto plano; el caso de uso la hashea vía {@link develope.auth.application.port.out.PasswordHasherPort}
 */
public record RegisterUserCommand(String username, String rawPassword) {}
