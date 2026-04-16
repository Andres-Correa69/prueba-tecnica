package develope.cars.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import develope.cars.infrastructure.adapter.in.rest.dto.ErrorResponse;
import develope.cars.infrastructure.config.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.List;

/**
 * Cadena de seguridad para cars-service.
 *
 * <p>Puntos clave a explicar durante el examen:</p>
 * <ul>
 *   <li>Sesión stateless — cada petición debe llevar un JWT, no hay
 *       estado de sesión en el servidor.</li>
 *   <li>CSRF deshabilitado — no usamos cookies, así que CSRF no aplica.</li>
 *   <li>{@link JwtAuthenticationFilter} corre ANTES del filtro por defecto de
 *       usuario/contraseña de Spring para que el principal esté disponible al momento
 *       en que se evalúan las reglas de autorización.</li>
 *   <li>401 y 403 se devuelven como JSON (alineado con el resto de la forma
 *       de la API) vía entry-point / access-denied handler personalizados.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper json;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter, ObjectMapper json) {
        this.jwtFilter = jwtFilter;
        this.json = json;
    }

    @Bean
    public SecurityFilterChain carsFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                        .accessDeniedHandler(forbiddenHandler()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> writeJson(request, response,
                HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "authentication required");
    }

    private AccessDeniedHandler forbiddenHandler() {
        return (request, response, accessDeniedException) -> writeJson(request, response,
                HttpStatus.FORBIDDEN, "FORBIDDEN", "not allowed");
    }

    private void writeJson(HttpServletRequest req, HttpServletResponse res,
                           HttpStatus status, String code, String message) throws java.io.IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(code, message, Instant.now(), req.getRequestURI(), List.of());
        json.writeValue(res.getOutputStream(), body);
    }
}
