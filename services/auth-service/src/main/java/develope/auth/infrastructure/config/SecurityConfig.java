package develope.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security para auth-service.
 *
 * <p>Intención: este servicio solo expone endpoints <em>públicos</em>
 * ({@code /auth/register}, {@code /auth/login}). No hay área protegida,
 * así que convertimos la cadena de filtros en un pipeline simple y stateless que
 * solo deja pasar todo, manteniendo los beneficios de la presencia de
 * Spring Security (CSRF deshabilitado para uso de API, sin sesión).</p>
 *
 * <p>La emisión real del JWT ocurre dentro de {@code JwtTokenIssuerAdapter}; NO
 * lo enchufamos a la cadena de seguridad — el login es solo un POST regular
 * que devuelve un token en el cuerpo.</p>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception {
        return http
                // Somos una API stateless — sin cookies, sin round-trip de token CSRF.
                .csrf(AbstractHttpConfigurer::disable)
                // Dejar pasar el preflight (CORS) del navegador.
                .cors(cors -> {})
                // Sin sesión HTTP — cada petición es autocontenida.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/actuator/health").permitAll()
                        .anyRequest().denyAll())
                // Deshabilitar el formulario de login / prompts de basic-auth por defecto.
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }
}
