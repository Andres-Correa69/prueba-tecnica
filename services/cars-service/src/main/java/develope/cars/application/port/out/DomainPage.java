package develope.cars.application.port.out;

import java.util.List;

/**
 * Resultado paginado agnóstico al framework.
 *
 * <p>Deliberadamente NO exponemos el {@code Page} de Spring Data a través de
 * la capa de aplicación — hacerlo filtraría Spring dentro del dominio. El
 * adaptador construye este tipo a partir de {@code Page<T>}, y el adaptador
 * REST lo convierte en una respuesta JSON.</p>
 */
public record DomainPage<T>(
        List<T> content,
        int page,
        int size,
        long total
) {}
