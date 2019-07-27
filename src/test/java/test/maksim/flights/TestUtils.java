package test.maksim.flights;

import test.maksim.flights.domain.FlightsRequest;
import test.maksim.flights.domain.Route;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils {

    public FlightsRequest createFlightsRequest(int maxStops,
                                               String from,
                                               String to) {
        return FlightsRequest.builder()
                .maxStops(maxStops)
                .departureAirport(from)
                .arrivalAirport(to)
                .build();
    }

    public Route createRoute(String from,
                             String to) {
        var route = new Route();
        route.setAirportFrom(from);
        route.setAirportTo(to);

        return route;
    }
}
