package test.maksim.flights.service;

import test.maksim.flights.Config;
import test.maksim.flights.domain.Route;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;
import test.maksim.flights.constants.OperatorNames;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoutesRequestServiceTest {

    @InjectMocks
    private RoutesRequestService service;

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Config config;

    @Test
    public void request_nothingFound_shouldReturnEmptyList() {
        mockRestTemplate(null);

        List<Route> routes = service.request(null, List.of(OperatorNames.RYANAIR));

        assertThat(routes, hasSize(0));
    }

    @Test
    public void request_3foundBut1Matches_shouldReturnSingleRoute() {
        var route1 = createRoute(null, OperatorNames.RYANAIR);
        var route2 = createRoute(null, "unknown");
        var route3 = createRoute("some airport", OperatorNames.RYANAIR);
        mockRestTemplate(new Route[]{route1, route2, route3});

        List<Route> routes = service.request(null, List.of(OperatorNames.RYANAIR));

        assertThat(routes, equalTo(List.of(route1)));
    }

    // Util methods

    private Route createRoute(String connectingAirport,
                              String operator) {
        var route = new Route();
        route.setConnectingAirport(connectingAirport);
        route.setOperator(operator);

        return route;
    }

    private void mockRestTemplate(Route[] routes) {
        var serviceUrl = "url";
        when(config.getRoutesServiceUrl()).thenReturn(serviceUrl);
        when(restTemplate.getForObject(serviceUrl, Route[].class)).thenReturn(routes);
    }
}