package develope.auth.infrastructure.adapter.out.security;

import develope.auth.application.port.out.PasswordHasherPort;
import develope.auth.domain.vo.PasswordHash;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Implementación BCrypt de {@link PasswordHasherPort}.
 *
 * <p>¿Por qué BCrypt (y no SHA-256, ni MD5)?</p>
 * <ul>
 *   <li>Deliberadamente <strong>lento</strong>: factor de costo adaptativo para que podamos
 *       aumentar el work factor conforme mejora el hardware.</li>
 *   <li>Salt aleatorio incorporado por contraseña — las rainbow tables no ayudan.</li>
 *   <li>El formato de salida codifica algoritmo + costo + salt + hash, así que la rotación
 *       es trivial (basta con re-hashear en el siguiente login si cambió el costo).</li>
 * </ul>
 */
@Component
public class BCryptPasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder encoder;

    public BCryptPasswordHasherAdapter(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public PasswordHash hash(String rawPassword) {
        return PasswordHash.of(encoder.encode(rawPassword));
    }

    @Override
    public boolean matches(String rawPassword, PasswordHash stored) {
        return encoder.matches(rawPassword, stored.value());
    }
}
