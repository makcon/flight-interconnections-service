package test.maksim.flights.builder;

import test.maksim.flights.TestUtils;
import test.maksim.flights.domain.InterconnectingRoute;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class OneStopRouteBuilderTest {

    private final OneStopRouteBuilder builder = new OneStopRouteBuilder();

    @Test
    public void build_has1InterconnectingFlights_shouldReturn2Routes() {
        var request = TestUtils.createFlightsRequest(1, "a", "e");
        var  route1 = TestUtils.createRoute("a", "b");
        var  route2 = TestUtils.createRoute("a", "e");
        var  route3 = TestUtils.createRoute("b", "e");
        var  route4 = TestUtils.createRoute("a", "c");

        List<InterconnectingRoute> result = builder.build(request, List.of(route1, route2, route3, route4));

        assertThat(result, notNullValue());
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getRoutes(), equalTo(List.of(route1, route3)));
    }
}