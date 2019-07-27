package test.maksim.flights.builder;

import test.maksim.flights.domain.FlightsRequest;
import test.maksim.flights.domain.InterconnectingRoute;
import test.maksim.flights.domain.Route;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InterconnectingRoutesBuilderTest {

    @InjectMocks
    private InterconnectingRoutesBuilder builder;

    @Mock
    private DirectRouteBuilder directRouteBuilder;
    @Mock
    private OneStopRouteBuilder oneStopRouteBuilder;

    @Test
    public void build_maxStops0_noDirectFlight_shouldReturnEmptyList() {
        var  request = createRequest(0);
        mockDirectRouteBuilder(null);

        List<InterconnectingRoute> result = builder.build(request, List.of(mock(Route.class)));

        assertThat(result, notNullValue());
        assertThat(result, hasSize(0));
    }

    @Test
    public void build_maxStops0_hasDirectFlight_shouldReturnSingleRoute() {
        var  request = createRequest(0);
        var  interconnectingRoute = mock(InterconnectingRoute.class);
        mockDirectRouteBuilder(interconnectingRoute);

        List<InterconnectingRoute> result = builder.build(request, List.of(mock(Route.class)));

        assertThat(result, equalTo(List.of(interconnectingRoute)));
    }

    @Test
    public void build_maxStops1_hasDirectFlightAndInterconnectingRoute_shouldReturn2Routes() {
        var  request = createRequest(1);
        var interconnectingRoute1 = mock(InterconnectingRoute.class);
        var interconnectingRoute2 = mock(InterconnectingRoute.class);
        mockDirectRouteBuilder(interconnectingRoute1);
        mockOneStopRouteBuilder(interconnectingRoute2);

        List<InterconnectingRoute> result = builder.build(request, List.of(mock(Route.class)));

        assertThat(result, equalTo(List.of(interconnectingRoute1, interconnectingRoute2)));
    }

    // Util methods

    private void mockDirectRouteBuilder(InterconnectingRoute route) {
        when(directRouteBuilder.build(ArgumentMatchers.any(), anyList())).thenReturn(Optional.ofNullable(route));
    }

    private void mockOneStopRouteBuilder(InterconnectingRoute route) {
        when(oneStopRouteBuilder.build(ArgumentMatchers.any(), anyList())).thenReturn(List.of(route));
    }

    private FlightsRequest createRequest(int maxStops) {
        return FlightsRequest.builder()
                .maxStops(maxStops)
                .build();
    }
}