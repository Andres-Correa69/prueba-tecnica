package develope.cars.infrastructure.adapter.out.persistence;

import develope.cars.application.port.in.CarFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Factorías de Specification null-safe para el endpoint de listar carros.
 *
 * <p>Cada predicado devuelve {@code null} cuando su filtro de entrada es null;
 * Spring Data interpreta un predicado null como "sin restricción". Eso mantiene
 * la API extremadamente simple: el controlador solo pasa el filtro tal cual.</p>
 */
final class CarSpecifications {

    private CarSpecifications() {}

    static Specification<CarJpaEntity> ownedBy(UUID userId) {
        return (root, cq, cb) -> cb.equal(root.get("userId"), userId);
    }

    static Specification<CarJpaEntity> fromFilter(CarFilter f) {
        return Specification.allOf(
                placaLike(f.placa()),
                modeloLike(f.modelo()),
                marcaEq(f.marca()),
                anioEq(f.anio())
        );
    }

    private static Specification<CarJpaEntity> placaLike(String placa) {
        if (placa == null || placa.isBlank()) return null;
        String needle = "%" + placa.trim().toUpperCase() + "%";
        return (root, cq, cb) -> cb.like(cb.upper(root.get("placa")), needle);
    }

    private static Specification<CarJpaEntity> modeloLike(String modelo) {
        if (modelo == null || modelo.isBlank()) return null;
        String needle = "%" + modelo.trim().toLowerCase() + "%";
        return (root, cq, cb) -> cb.like(cb.lower(root.get("modelo")), needle);
    }

    private static Specification<CarJpaEntity> marcaEq(String marca) {
        if (marca == null || marca.isBlank()) return null;
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("marca")), marca.trim().toLowerCase());
    }

    private static Specification<CarJpaEntity> anioEq(Integer anio) {
        if (anio == null) return null;
        return (root, cq, cb) -> cb.equal(root.get("anio"), anio);
    }
}
