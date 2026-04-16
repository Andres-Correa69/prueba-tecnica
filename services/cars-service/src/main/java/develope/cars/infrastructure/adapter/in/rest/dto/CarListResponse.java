package develope.cars.infrastructure.adapter.in.rest.dto;

import develope.cars.application.port.out.DomainPage;
import develope.cars.domain.model.Car;

import java.util.List;

public record CarListResponse(
        List<CarResponse> content,
        int page,
        int size,
        long total
) {
    public static CarListResponse from(DomainPage<Car> page) {
        return new CarListResponse(
                page.content().stream().map(CarResponse::from).toList(),
                page.page(),
                page.size(),
                page.total());
    }
}
