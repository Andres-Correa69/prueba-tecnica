package develope.cars.application.port.out;

/**
 * Petición de paginación agnóstica al framework que usa la capa de aplicación para
 * pedirle al puerto del repositorio una porción de resultados.
 *
 * @param page número de página base-cero.
 * @param size tamaño de página (acotado en el servidor para que los clientes no puedan pedir 1M de filas).
 * @param sort spec de ordenamiento opcional de una sola columna, formato {@code "field,asc"} o {@code "field,desc"}.
 */
public record PageRequest(int page, int size, String sort) {
    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1 || size > 100) throw new IllegalArgumentException("size must be 1..100");
    }
}
