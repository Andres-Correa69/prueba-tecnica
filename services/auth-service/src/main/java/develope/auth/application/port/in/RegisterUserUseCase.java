package develope.auth.application.port.in;

import java.util.UUID;

/**
 * Puerto de entrada expuesto por el bounded context de auth para REGISTRAR un nuevo usuario.
 *
 * <p>¿Por qué interfaces para los casos de uso (aun si hay una sola implementación hoy)?
 * Dibujan una línea explícita entre "lo que la aplicación puede hacer" (puertos) y
 * "cómo lo hace" (servicios). Los tests del adaptador REST pueden mockear esta
 * interfaz sin cargar Spring, y algún día podríamos intercambiar la
 * implementación (por ejemplo, añadir un decorador de caché) sin cambios en REST.</p>
 */
public interface RegisterUserUseCase {

    /**
     * Registra un nuevo usuario.
     *
     * @return el id generado del usuario.
     * @throws develope.auth.domain.exception.UsernameAlreadyExistsException
     *         cuando el nombre de usuario ya está tomado.
     * @throws IllegalArgumentException cuando el usuario / contraseña fallan
     *         la validación del dominio.
     */
    UUID register(RegisterUserCommand command);
}
