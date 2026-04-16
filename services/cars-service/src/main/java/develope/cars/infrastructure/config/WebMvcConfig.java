package develope.cars.infrastructure.config;

import develope.cars.infrastructure.config.security.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Registra el {@link CurrentUserArgumentResolver} en Spring MVC para que
 * los parámetros {@code @CurrentUser AuthenticatedUser user} funcionen en cada
 * handler de controlador.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver resolver;

    public WebMvcConfig(CurrentUserArgumentResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }
}
