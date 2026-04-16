package develope.cars.application.port.in;

/**
 * Filtros de búsqueda opcionales para listar carros.
 *
 * <p>Todos los campos son nullable — ausente = sin restricción sobre ese campo. El
 * adaptador de persistencia traduce estos filtros en una {@code Specification}
 * de JPA (predicados null-safe); ver {@code CarSpecifications}.</p>
 */
public record CarFilter(
        String placa,
        String modelo,
        Integer anio,
        String marca
) {
    public static CarFilter empty() {
        return new CarFilter(null, null, null, null);
    }
}
