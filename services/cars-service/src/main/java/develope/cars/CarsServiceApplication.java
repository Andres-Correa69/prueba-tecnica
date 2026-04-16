package develope.cars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio cars-service.
 *
 * <p>Este servicio es dueño de la tabla {@code cars.Cars} y nunca se comunica
 * directamente con auth-service — confía en los JWTs firmados con HS256 que
 * produce auth-service usando el {@code JWT_SECRET} compartido.</p>
 */
@SpringBootApplication
public class CarsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarsServiceApplication.class, args);
    }
}
