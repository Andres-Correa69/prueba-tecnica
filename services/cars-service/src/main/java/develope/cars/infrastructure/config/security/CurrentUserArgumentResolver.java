package develope.cars.infrastructure.config.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolver de Spring MVC que resuelve los parámetros {@code @CurrentUser AuthenticatedUser user}
 * leyendo el principal establecido por {@code JwtAuthenticationFilter}.
 *
 * <p>Mantener el resolver en la capa de infra permite que los controladores permanezcan
 * completamente ajenos a Spring Security — solo declaran un parámetro y
 * obtienen un DTO inmutable.</p>
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && AuthenticatedUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mav,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser principal)) {
            // No debería pasar — SecurityConfig garantiza que el filtro corrió para las rutas protegidas.
            return WebArgumentResolver.UNRESOLVED;
        }
        return principal;
    }
}
