package develope.cars.infrastructure.config.security;

import develope.cars.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Wrapper delgado alrededor del parser de jjwt que valida firma, issuer y
 * expiración en una sola llamada.
 *
 * <p>Cualquier problema con el token (firma inválida, issuer incorrecto, expirado,
 * mal formado) se expone como una subclase de {@link io.jsonwebtoken.JwtException} —
 * el filtro la captura y devuelve 401.</p>
 */
@Component
public class JwtDecoder {

    private final io.jsonwebtoken.JwtParser parser;

    public JwtDecoder(JwtProperties props) {
        byte[] secretBytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "security.jwt.secret must be >= 32 bytes (256 bits) for HS256");
        }
        SecretKey key = new SecretKeySpec(secretBytes, "HmacSHA256");
        this.parser = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.issuer())
                .build();
    }

    /**
     * Parsea + valida el token. Al retornar, la firma, el issuer y la
     * expiración ya se saben correctos.
     */
    public Claims decode(String jwt) {
        return parser.parseSignedClaims(jwt).getPayload();
    }
}
