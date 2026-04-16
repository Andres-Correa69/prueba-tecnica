package develope.auth.infrastructure.adapter.out.security;

import develope.auth.application.port.out.TokenIssuerPort;
import develope.auth.domain.model.User;
import develope.auth.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Adaptador que emite JWT HS256 usando jjwt 0.12.x.
 *
 * <p>Claims emitidos:</p>
 * <ul>
 *   <li>{@code sub} — id del usuario (UUID como string). Usado por cars-service para
 *       acotar cada consulta a {@code user_id = sub}.</li>
 *   <li>{@code username} — conveniencia para la UI.</li>
 *   <li>{@code iat} — issued-at (emitido en).</li>
 *   <li>{@code exp} — expires-at (expira en, configurable vía {@code security.jwt.expiration-minutes}).</li>
 *   <li>{@code iss} — nombre del emisor; cars-service rechaza tokens de otros emisores.</li>
 * </ul>
 *
 * <p>Algoritmo: HS256 (simétrico). Ambos servicios comparten {@code JWT_SECRET}.
 * Para un sistema en producción entre equipos probablemente actualizarías a RS256 +
 * JWKS (asimétrico) para que cars-service no necesite la clave de firma — ver
 * {@code docs/03-seguridad-jwt.md}.</p>
 */
@Component
public class JwtTokenIssuerAdapter implements TokenIssuerPort {

    private final JwtProperties props;
    private final SecretKey signingKey;

    public JwtTokenIssuerAdapter(JwtProperties props) {
        this.props = props;
        byte[] secretBytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            // HS256 requiere >=256 bits de material de clave — cualquier cosa menor es
            // insegura. Fallar rápido al arranque para nunca correr con una clave débil.
            throw new IllegalStateException(
                    "security.jwt.secret must be >= 32 bytes (256 bits) for HS256");
        }
        this.signingKey = new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    @Override
    public IssuedToken issue(User user) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant exp = now.plusSeconds(props.expirationMinutes() * 60L);

        String token = Jwts.builder()
                .issuer(props.issuer())
                .subject(user.id().value().toString())
                .claim("username", user.username().value())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        return new IssuedToken(token, exp);
    }
}
