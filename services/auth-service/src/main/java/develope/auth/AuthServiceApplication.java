package develope.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio auth-service.
 *
 * <p>Nota hexagonal: esta clase es lo único específico de Spring en la
 * "cima" del módulo. Todo lo que está dentro de {@code develope.auth.domain} y
 * {@code develope.auth.application} debe permanecer agnóstico al framework; las
 * anotaciones de Spring solo aparecen en {@code develope.auth.infrastructure}.</p>
 *
 * <p>El escaneo de componentes está acotado a {@code develope.auth} para que el
 * paquete hermano {@code develope.cars} (que de todos modos se empaqueta por
 * separado) no pueda ser recogido por accidente.</p>
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
