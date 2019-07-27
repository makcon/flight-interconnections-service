package test.maksim.flights.builder;

import test.maksim.flights.domain.FlightsRequest;
import test.maksim.flights.domain.InterconnectingRoute;
import test.maksim.flights.domain.Route;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class OneStopRouteBuilder {

    public List<InterconnectingRoute> build(FlightsRequest request,
                                            List<Route> routes) {
        List<InterconnectingRoute> result = new ArrayList<>();
        for (var route : routes) {
            if (Objects.equals(route.getAirportFrom(), request.getDepartureAirport())) {
                routes.stream()
                        .filter(it -> Objects.equals(it.getAirportFrom(), route.getAirportTo()) && Objects.equals(it.getAirportTo(), request.getArrivalAirport()))
                        .findFirst()
                        .ifPresent(it -> result.add(new InterconnectingRoute(List.of(route, it))));
            }
        }

        return result;
    }
}
