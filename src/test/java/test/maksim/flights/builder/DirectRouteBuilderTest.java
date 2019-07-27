package test.maksim.flights.builder;

import test.maksim.flights.TestUtils;
import test.maksim.flights.domain.FlightsRequest;
import test.maksim.flights.domain.InterconnectingRoute;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DirectRouteBuilderTest {

    private final DirectRouteBuilder builder = new DirectRouteBuilder();

    @Test
    public void build_noDirectFlight_shouldReturnEmpty() {
        FlightsRequest request = TestUtils.createFlightsRequest(0, "a", "e");
        var route1 = TestUtils.createRoute("a", "b");

        Optional<InterconnectingRoute> result = builder.build(request, List.of(route1));

        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void build_hasDirectFlight_shouldReturnRoute() {
        FlightsRequest request = TestUtils.createFlightsRequest(1, "a", "e");
        var route1 = TestUtils.createRoute("a", "b");
        var route2 = TestUtils.createRoute("a", "e");

        Optional<InterconnectingRoute> result = builder.build(request, List.of(route1, route2));

        assertThat(result.isPresent(), is(true));
        assertThat(result.get().getRoutes(), equalTo(singletonList(route2)));
    }


}