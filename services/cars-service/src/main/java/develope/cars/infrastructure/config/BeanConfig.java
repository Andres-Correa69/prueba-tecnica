package develope.cars.infrastructure.config;

import develope.cars.application.port.in.CreateCarUseCase;
import develope.cars.application.port.in.DeleteCarUseCase;
import develope.cars.application.port.in.GetCarUseCase;
import develope.cars.application.port.in.ListCarsByOwnerUseCase;
import develope.cars.application.port.in.UpdateCarUseCase;
import develope.cars.application.port.out.CarRepositoryPort;
import develope.cars.application.usecase.CreateCarService;
import develope.cars.application.usecase.DeleteCarService;
import develope.cars.application.usecase.GetCarService;
import develope.cars.application.usecase.ListCarsByOwnerService;
import develope.cars.application.usecase.UpdateCarService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Cableado manual de los 5 casos de uso de carros. */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class BeanConfig {

    @Bean public CreateCarUseCase createCarUseCase(CarRepositoryPort cars) { return new CreateCarService(cars); }
    @Bean public UpdateCarUseCase updateCarUseCase(CarRepositoryPort cars) { return new UpdateCarService(cars); }
    @Bean public DeleteCarUseCase deleteCarUseCase(CarRepositoryPort cars) { return new DeleteCarService(cars); }
    @Bean public GetCarUseCase getCarUseCase(CarRepositoryPort cars) { return new GetCarService(cars); }
    @Bean public ListCarsByOwnerUseCase listCarsByOwnerUseCase(CarRepositoryPort cars) { return new ListCarsByOwnerService(cars); }
}
