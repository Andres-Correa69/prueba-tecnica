package develope.auth.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configuración de CORS.
 *
 * <p>El dev server de Angular (`ng serve`) corre en {@code http://localhost:4200};
 * sin CORS el navegador bloquearía cada XHR desde ahí. Mantenemos los orígenes
 * permitidos acotados (no {@code *}) incluso en dev, para detectar configuraciones
 * incorrectas temprano.</p>
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(allowedOrigins);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setExposedHeaders(List.of("Location"));
        cfg.setAllowCredentials(false); // sin cookies → no se necesitan credentials
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(source);
    }
}
