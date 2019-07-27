package test.maksim.flights.builder;

import test.maksim.flights.domain.FlightsRequest;
import test.maksim.flights.domain.InterconnectingRoute;
import test.maksim.flights.domain.Route;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.singletonList;

@Component
public class DirectRouteBuilder {

    public Optional<InterconnectingRoute> build(FlightsRequest request,
                                                List<Route> routes) {
        return routes.stream()
                .filter(it -> isDirect(request, it))
                .findFirst()
                .flatMap(it -> Optional.of(new InterconnectingRoute(singletonList(it))));
    }

    private boolean isDirect(FlightsRequest request,
                             Route route) {
        return Objects.equals(route.getAirportFrom(), request.getDepartureAirport())
                && Objects.equals(route.getAirportTo(), request.getArrivalAirport());
    }
}
