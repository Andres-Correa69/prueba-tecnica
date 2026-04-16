package develope.auth.infrastructure.config;

import develope.auth.application.port.in.LoginUseCase;
import develope.auth.application.port.in.RegisterUserUseCase;
import develope.auth.application.port.out.PasswordHasherPort;
import develope.auth.application.port.out.TokenIssuerPort;
import develope.auth.application.port.out.UserRepositoryPort;
import develope.auth.application.usecase.LoginService;
import develope.auth.application.usecase.RegisterUserService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cableado central y <em>manual</em> de la capa de aplicación.
 *
 * <p>Decisión de diseño: ninguno de los casos de uso está anotado con {@code @Service}.
 * Los ensamblamos aquí, en un solo archivo, para que el grafo de dependencias sea
 * completamente visible. Para una base de código pequeña esto es mucho más legible que el
 * autodescubrimiento dirigido por anotaciones, y mantiene los paquetes
 * {@code application} y {@code domain} libres del framework.</p>
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class BeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Costo 10 es un valor por defecto razonable para 2025: ~70ms por hash en hardware moderno.
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepositoryPort users,
                                                   PasswordHasherPort hasher) {
        return new RegisterUserService(users, hasher);
    }

    @Bean
    public LoginUseCase loginUseCase(UserRepositoryPort users,
                                     PasswordHasherPort hasher,
                                     TokenIssuerPort tokenIssuer) {
        return new LoginService(users, hasher, tokenIssuer);
    }
}
