package test.maksim.flights.rest;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import test.maksim.flights.Config;
import test.maksim.flights.domain.FlightsRequest;
import test.maksim.flights.rest.dto.Flight;
import test.maksim.flights.service.FlightsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
@Slf4j
@Api
public class FlightsController {

    /**
     * TODO future improvements: add exception handling.
     */

    private final AsyncListenableTaskExecutor serviceExecutor;
    private final FlightsService service;
    private final Config config;

    @GetMapping("/interconnections")
    public List<Flight> getInterconnections(@RequestParam("departure") String departure,
                                            @RequestParam("arrival") String arrival,
                                            @RequestParam("departureDateTime") String departureDateTime,
                                            @RequestParam("arrivalDateTime") String arrivalDateTime,
                                            @RequestParam(value = "maxStops", required = false) Integer maxStops) {
        var request = FlightsRequest.builder()
                .departureAirport(departure)
                .arrivalAirport(arrival)
                .departureDateTime(LocalDateTime.parse(departureDateTime))
                .arrivalDateTime(LocalDateTime.parse(arrivalDateTime))
                .maxStops(maxStops == null ? config.getDefaultMaxStops() : maxStops)
                .build();
        log.info("Received request: {}", request);

        return service.getFlights(request);
    }

    @GetMapping("/interconnections/async")
    public ListenableFuture<List<Flight>> getInterconnectionsAsync(@RequestParam("departure") String departure,
                                                                   @RequestParam("arrival") String arrival,
                                                                   @RequestParam("departureDateTime") String departureDateTime,
                                                                   @RequestParam("arrivalDateTime") String arrivalDateTime,
                                                                   @RequestParam(value = "maxStops", required = false) Integer maxStops) {
        var request = FlightsRequest.builder()
                .departureAirport(departure)
                .arrivalAirport(arrival)
                .departureDateTime(LocalDateTime.parse(departureDateTime))
                .arrivalDateTime(LocalDateTime.parse(arrivalDateTime))
                .maxStops(maxStops == null ? config.getDefaultMaxStops() : maxStops)
                .build();
        log.info("Received request: {}", request);

        return serviceExecutor.submitListenable(() -> service.getFlights(request));
    }
}
