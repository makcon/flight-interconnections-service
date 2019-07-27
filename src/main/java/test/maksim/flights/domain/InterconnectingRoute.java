package test.maksim.flights.domain;

import lombok.Data;

import java.util.List;

@Data
public class InterconnectingRoute {

    private final List<Route> routes;
}
