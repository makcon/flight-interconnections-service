package test.maksim.flights.service;

import test.maksim.flights.Config;
import test.maksim.flights.builder.FlightsBuilder;
import test.maksim.flights.builder.InterconnectingRoutesBuilder;
import test.maksim.flights.domain.*;
import test.maksim.flights.rest.dto.Flight;
import test.maksim.flights.validator.RequestValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static test.maksim.flights.constants.OperatorNames.RYANAIR;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightsService {

    private final RoutesRequestService routesRequestService;
    private final SchedulesRequestService schedulesRequestService;
    private final InterconnectingRoutesBuilder interconnectingRoutesBuilder;
    private final RequestValidator requestValidator;
    private final FlightsBuilder flightsBuilder;
    private final Config config;
    private final AsyncTaskExecutor schedulesExecutor;

    public List<Flight> getFlights(FlightsRequest request) {
        requestValidator.validate(request);

        List<Route> routes = routesRequestService.request(null, singleton(RYANAIR));
        log.info("Received {} routes", routes.size());
        if (routes.isEmpty()) {
            return emptyList();
        }

        List<InterconnectingRoute> interconnectingRoutes = interconnectingRoutesBuilder.build(request, routes);
        log.debug("Route map: {}", interconnectingRoutes);

        return requestSchedulesAndBuildFlights(request, interconnectingRoutes);
    }

    private List<Flight> requestSchedulesAndBuildFlights(FlightsRequest request,
                                                         List<InterconnectingRoute> interconnectingRoutes) {
        Map<InterconnectingRoute, List<Flight>> routeToFlightMap = new HashMap<>();
        interconnectingRoutes.stream()
                .map(it -> schedulesExecutor.submit(() -> requestAndBuildOneRoute(request, routeToFlightMap, it)))
                .collect(toList())
                .forEach(it -> {
                    try {
                        it.get();
                    } catch (Exception e) {
                        log.error("Failed to handle route", e);
                    }
                });

        log.info("Finish to build routes for: {}", request);
        return routeToFlightMap.values().stream()
                .flatMap(Collection::stream)
                .filter(it -> it.getStops() == it.getLegs().size() - 1)
                .sorted(Comparator.comparingInt(Flight::getStops))
                .collect(toList());
    }

    private InterconnectingRoute requestAndBuildOneRoute(FlightsRequest request,
                                                         Map<InterconnectingRoute, List<Flight>> routeToFlightMap,
                                                         InterconnectingRoute interconnectingRoute) {
        LocalDateTime nextTime = null;
        for (var route : interconnectingRoute.getRoutes()) {
            if (nextTime == null) {
                nextTime = request.getDepartureDateTime();
            }

            var scheduleRequest = ScheduleRequest.builder()
                    .from(route.getAirportFrom())
                    .to(route.getAirportTo())
                    .dateTime(nextTime)
                    .dateTimeEnd(request.getArrivalDateTime())
                    .build();

            log.debug("Sending schedule request: {}", scheduleRequest);
            List<FlightSchedule> flightSchedules = schedulesRequestService.request(scheduleRequest);
            log.debug("Received schedules: {}", flightSchedules);

            if (flightSchedules.isEmpty()) {
                log.debug("flightSchedules are empty, removing route");
                routeToFlightMap.remove(interconnectingRoute);
                break;
            }

            List<Flight> flights = routeToFlightMap.get(interconnectingRoute);
            if (flights == null) {
                flights = flightsBuilder.createFlights(
                        request,
                        interconnectingRoute.getRoutes().size() - 1,
                        route,
                        flightSchedules
                );
                log.debug("Primary flight size: {}", flights.size());
                routeToFlightMap.put(interconnectingRoute, flights);
            } else {
                flightsBuilder.addAvailableLegs(route, flightSchedules, flights);
            }

            nextTime = calculateNextTime(flightSchedules);
        }

        return interconnectingRoute;
    }

    private LocalDateTime calculateNextTime(List<FlightSchedule> flightSchedules) {
        var minArrivalTime = flightSchedules.stream()
                .map(FlightSchedule::getArrivalTime)
                .min(LocalDateTime::compareTo)
                .orElseThrow();

        return minArrivalTime.plus(config.getMinStopDuration(), HOURS);
    }
}
