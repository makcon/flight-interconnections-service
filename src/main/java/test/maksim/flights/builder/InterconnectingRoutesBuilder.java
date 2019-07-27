package test.maksim.flights.builder;

import test.maksim.flights.domain.FlightsRequest;
import test.maksim.flights.domain.InterconnectingRoute;
import test.maksim.flights.domain.Route;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InterconnectingRoutesBuilder {

    /**
     * TODO future improvements: in order to handle unlimited stops
     * need to build interconnecting routes with more smart algorithm,
     * e.q. Graph data structure
     */

    private final DirectRouteBuilder directRouteBuilder;
    private final OneStopRouteBuilder oneStopRouteBuilder;

    public List<InterconnectingRoute> build(FlightsRequest request,
                                            List<Route> routes) {
        List<InterconnectingRoute> result = new ArrayList<>();

        directRouteBuilder.build(request, routes).ifPresent(result::add);

        if (request.getMaxStops() == 1) {
            result.addAll(oneStopRouteBuilder.build(request, routes));
        }

        return result;
    }
}
