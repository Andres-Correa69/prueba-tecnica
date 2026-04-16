package develope.cars.infrastructure.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import develope.cars.infrastructure.adapter.in.rest.dto.ErrorResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Filtro de autenticación JWT stateless.
 *
 * <p>Flujo (comentado paso a paso):</p>
 * <ol>
 *   <li>Authorization ausente o no-Bearer → proceder sin autenticación.
 *       Más adelante {@code SecurityConfig} denegará la petición con 401.</li>
 *   <li>Parsear y validar el token vía {@link JwtDecoder} — maneja firma,
 *       issuer y expiración.</li>
 *   <li>Construir {@link AuthenticatedUser} a partir de los claims {@code sub} + {@code username}
 *       e instalarlo en el {@link SecurityContextHolder}.</li>
 *   <li>Cualquier excepción de JWT → devolver 401 con un cuerpo JSON uniforme y abortar.</li>
 * </ol>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder decoder;
    private final ObjectMapper json;

    public JwtAuthenticationFilter(JwtDecoder decoder, ObjectMapper json) {
        this.decoder = decoder;
        this.json = json;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // El probe de health es público; se salta la validación del token por completo para
        // que ops / los balanceadores de carga puedan llamarlo sin autenticación.
        return "/actuator/health".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            // Sin token → dejar que la cadena continúe; SecurityConfig deniega.
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        try {
            Claims claims = decoder.decode(token);

            UUID userId = UUID.fromString(claims.getSubject());
            String username = claims.get("username", String.class);

            AuthenticatedUser principal = new AuthenticatedUser(userId, username);

            // Las authorities están vacías — no usamos autorización basada en roles
            // en esta demo. Si se agregaran roles, serían un claim y se
            // mapearían aquí a instancias de SimpleGrantedAuthority.
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException invalid) {
            // Importante: limpiar el contexto de seguridad para que una autenticación
            // parcialmente establecida no se filtre a la siguiente petición en el mismo hilo.
            SecurityContextHolder.clearContext();
            writeUnauthorized(request, response, "INVALID_TOKEN", invalid.getMessage());
        }
    }

    private void writeUnauthorized(HttpServletRequest req, HttpServletResponse res,
                                   String code, String message) throws IOException {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(
                code,
                message == null ? "invalid token" : message,
                Instant.now(),
                req.getRequestURI(),
                List.of());
        json.writeValue(res.getOutputStream(), body);
    }
}
