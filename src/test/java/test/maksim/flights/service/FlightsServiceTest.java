package test.maksim.flights.service;

import test.maksim.flights.Config;
import test.maksim.flights.TestUtils;
import test.maksim.flights.builder.FlightsBuilder;
import test.maksim.flights.builder.InterconnectingRoutesBuilder;
import test.maksim.flights.rest.dto.Flight;
import test.maksim.flights.validator.RequestValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.task.AsyncTaskExecutor;
import test.maksim.flights.constants.OperatorNames;
import test.maksim.flights.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FlightsServiceTest {

    private static final String AIRPORT_A = "a";
    private static final String AIRPORT_B = "b";
    private static final String AIRPORT_C = "c";

    private FlightsService service;

    @Mock
    private RoutesRequestService routesRequestService;
    @Mock
    private SchedulesRequestService schedulesRequestService;
    @Mock
    private InterconnectingRoutesBuilder interconnectingRoutesBuilder;
    @Mock
    private RequestValidator requestValidator;
    @Mock
    private Config config;
    @Mock
    private AsyncTaskExecutor schedulesExecutor;

    @Before
    public void setUp() {
        service = new FlightsService(
                routesRequestService,
                schedulesRequestService,
                interconnectingRoutesBuilder,
                requestValidator,
                new FlightsBuilder(config),
                config,
                schedulesExecutor
        );
    }

    @Test
    public void getFlights_validationError_shouldThrowException() {
        mockRoutesRequestValidatorException();

        try {
            service.getFlights(createFlightsRequest());
            Assert.fail();
        } catch (Exception e) {
            verifyRoutesRequestServiceCall(0);
            verifySchedulesRequestService(0);
        }
    }

    @Test
    public void getFlights_routeServiceReturnsEmptyList_shouldReturnEmptyResult() {
        mockRoutesRequestService(emptyList());

        List<Flight> flights = service.getFlights(createFlightsRequest());

        assertThat(flights, hasSize(0));
        verifyRoutesRequestServiceCall(1);
        verifySchedulesRequestService(0);
    }

    @Test
    public void getFlights_directFlights_shouldReturnOnlyDirect() {
        var routeDirect = TestUtils.createRoute(AIRPORT_A, AIRPORT_B);
        mockRoutesRequestService(List.of(routeDirect));
        var interconnectingRoute = new InterconnectingRoute(List.of(routeDirect));
        mockInterconnectingRoutesBuilder(List.of(interconnectingRoute));
        var flightsRequest = createFlightsRequest(
                LocalDateTime.parse("2019-07-01T07:00"),
                LocalDateTime.parse("2019-07-03T07:00")
        );
        var flightSchedule = new FlightSchedule(
                LocalDateTime.parse("2019-07-01T09:00"),
                LocalDateTime.parse("2019-07-01T10:00")
        );
        mockSchedulesRequestService(List.of(flightSchedule));
        mockSchedulesExecutor();

        List<Flight> flights = service.getFlights(flightsRequest);

        assertThat(flights, hasSize(1));
        var expectedLeg = Flight.Leg.builder()
                .departureAirport(AIRPORT_A)
                .arrivalAirport(AIRPORT_B)
                .departureDateTime(flightSchedule.getDepartureTime())
                .arrivalDateTime(flightSchedule.getArrivalTime())
                .build();
        verifyFlight(flights.get(0), List.of(expectedLeg));
        verifyRoutesRequestServiceCall(1);
        var scheduleRequest = ScheduleRequest.builder()
                .from(AIRPORT_A)
                .to(AIRPORT_B)
                .dateTime(flightsRequest.getDepartureDateTime())
                .dateTimeEnd(flightsRequest.getArrivalDateTime())
                .build();
        verifySchedulesRequestService(scheduleRequest);
    }

    @Test
    public void getFlights_interconnectingFlights_shouldReturnConnection() {
        var route1 = TestUtils.createRoute(AIRPORT_A, AIRPORT_B);
        var route2 = TestUtils.createRoute(AIRPORT_B, AIRPORT_C);
        mockRoutesRequestService(List.of(route1, route2));
        var interconnectingRoute = new InterconnectingRoute(List.of(route1, route2));
        mockInterconnectingRoutesBuilder(List.of(interconnectingRoute));
        var flightsRequest = createFlightsRequest(
                LocalDateTime.parse("2019-07-01T07:00"),
                LocalDateTime.parse("2019-07-03T07:00")
        );
        var flightSchedule1 = new FlightSchedule(
                LocalDateTime.parse("2019-07-01T09:00"),
                LocalDateTime.parse("2019-07-01T10:00")
        );
        var flightSchedule2 = new FlightSchedule(
                LocalDateTime.parse("2019-07-01T13:00"),
                LocalDateTime.parse("2019-07-01T14:00")
        );
        mockSchedulesRequestService(List.of(flightSchedule1), List.of(flightSchedule2));
        mockSchedulesExecutor();

        List<Flight> flights = service.getFlights(flightsRequest);

        assertThat(flights, hasSize(1));
        var expectedLeg1 = Flight.Leg.builder()
                .departureAirport(AIRPORT_A)
                .arrivalAirport(AIRPORT_B)
                .departureDateTime(flightSchedule1.getDepartureTime())
                .arrivalDateTime(flightSchedule1.getArrivalTime())
                .build();
        var expectedLeg2 = Flight.Leg.builder()
                .departureAirport(AIRPORT_B)
                .arrivalAirport(AIRPORT_C)
                .departureDateTime(flightSchedule2.getDepartureTime())
                .arrivalDateTime(flightSchedule2.getArrivalTime())
                .build();
        verifyFlight(flights.get(0), List.of(expectedLeg1, expectedLeg2));
        verifyRoutesRequestServiceCall(1);
    }

    @Test
    public void getFlights_interconnectingFlightsNoConnectionForSecondRoute_shouldReturnEmptyResult() {
        var route1 = TestUtils.createRoute(AIRPORT_A, AIRPORT_B);
        var route2 = TestUtils.createRoute(AIRPORT_B, AIRPORT_C);
        mockRoutesRequestService(List.of(route1, route2));
        var interconnectingRoute = new InterconnectingRoute(List.of(route1, route2));
        mockInterconnectingRoutesBuilder(List.of(interconnectingRoute));
        var flightsRequest = createFlightsRequest(
                LocalDateTime.parse("2019-07-01T07:00"),
                LocalDateTime.parse("2019-07-03T07:00")
        );
        var flightSchedule1 = new FlightSchedule(
                LocalDateTime.parse("2019-07-01T09:00"),
                LocalDateTime.parse("2019-07-01T10:00")
        );
        mockSchedulesRequestService(List.of(flightSchedule1), emptyList());

        List<Flight> flights = service.getFlights(flightsRequest);

        assertThat(flights, hasSize(0));
        verifyRoutesRequestServiceCall(1);
    }

    @Test
    public void getFlights_directAndInterconnectingFlights_shouldReturnDirectAndConnection() {
        var directRoute = TestUtils.createRoute(AIRPORT_A, AIRPORT_C);
        var route1 = TestUtils.createRoute(AIRPORT_A, AIRPORT_B);
        var route2 = TestUtils.createRoute(AIRPORT_B, AIRPORT_C);
        mockRoutesRequestService(List.of(route1, route2, directRoute));
        var interconnectingRouteDirect = new InterconnectingRoute(List.of(directRoute));
        var interconnectingRoute = new InterconnectingRoute(List.of(route1, route2));
        mockInterconnectingRoutesBuilder(List.of(interconnectingRouteDirect, interconnectingRoute));
        var flightsRequest = createFlightsRequest(
                LocalDateTime.parse("2019-07-01T07:00"),
                LocalDateTime.parse("2019-07-03T07:00")
        );
        var flightScheduleDirect = new FlightSchedule(
                LocalDateTime.parse("2019-07-01T09:30"),
                LocalDateTime.parse("2019-07-01T11:20")
        );
        var flightSchedule1 = new FlightSchedule(
                LocalDateTime.parse("2019-07-01T09:00"),
                LocalDateTime.parse("2019-07-01T10:00")
        );
        var flightSchedule2 = new FlightSchedule(
                LocalDateTime.parse("2019-07-01T13:00"),
                LocalDateTime.parse("2019-07-01T14:00")
        );
        mockSchedulesRequestService(List.of(flightScheduleDirect), List.of(flightSchedule1), List.of(flightSchedule2));
        mockSchedulesExecutor();

        List<Flight> flights = service.getFlights(flightsRequest);

        assertThat(flights, hasSize(2));
        var expectedLegDirect = Flight.Leg.builder()
                .departureAirport(AIRPORT_A)
                .arrivalAirport(AIRPORT_C)
                .departureDateTime(flightScheduleDirect.getDepartureTime())
                .arrivalDateTime(flightScheduleDirect.getArrivalTime())
                .build();
        var expectedLeg1 = Flight.Leg.builder()
                .departureAirport(AIRPORT_A)
                .arrivalAirport(AIRPORT_B)
                .departureDateTime(flightSchedule1.getDepartureTime())
                .arrivalDateTime(flightSchedule1.getArrivalTime())
                .build();
        var expectedLeg2 = Flight.Leg.builder()
                .departureAirport(AIRPORT_B)
                .arrivalAirport(AIRPORT_C)
                .departureDateTime(flightSchedule2.getDepartureTime())
                .arrivalDateTime(flightSchedule2.getArrivalTime())
                .build();
        verifyFlight(flights.get(0), List.of(expectedLegDirect));
        verifyFlight(flights.get(1), List.of(expectedLeg1, expectedLeg2));
        verifyRoutesRequestServiceCall(1);
    }

    // Util methods

    private FlightsRequest createFlightsRequest(LocalDateTime departureDateTime,
                                                LocalDateTime arrivalDateTime) {
        return FlightsRequest.builder()
                .departureDateTime(departureDateTime)
                .arrivalDateTime(arrivalDateTime)
                .build();
    }

    private FlightsRequest createFlightsRequest() {
        return FlightsRequest.builder().build();
    }

    private void mockRoutesRequestService(List<Route> routes) {
        when(routesRequestService.request(any(), any())).thenReturn(routes);
    }

    private void mockRoutesRequestValidatorException() {
        doThrow(RuntimeException.class).when(requestValidator).validate(any());
    }

    @SafeVarargs
    private void mockSchedulesRequestService(List<FlightSchedule> flightSchedule,
                                             List<FlightSchedule>... flightSchedules) {
        when(schedulesRequestService.request(any())).thenReturn(flightSchedule, flightSchedules);
    }

    private void mockInterconnectingRoutesBuilder(List<InterconnectingRoute> routes) {
        when(interconnectingRoutesBuilder.build(any(), anyList())).thenReturn(routes);
    }

    @SuppressWarnings("unchecked")
    private void mockSchedulesExecutor() {
        Mockito.when(schedulesExecutor.submit(argThat((ArgumentMatcher<Callable<InterconnectingRoute>>) argument -> {
            try {
                argument.call();
            } catch (Exception e) {
                Assert.fail();
            }
            return true;
        }))).thenReturn(Mockito.mock(Future.class));
    }

    private void verifyRoutesRequestServiceCall(int times) {
        verify(routesRequestService, times(times)).request(null, singleton(OperatorNames.RYANAIR));
    }

    private void verifySchedulesRequestService(int times) {
        verify(schedulesRequestService, times(times)).request(any());
    }

    private void verifySchedulesRequestService(ScheduleRequest request) {
        verify(schedulesRequestService).request(request);
    }

    private void verifyFlight(Flight flight,
                              List<Flight.Leg> legs) {
        assertThat(flight.getStops(), equalTo(legs.size() - 1));
        assertThat(flight.getLegs(), equalTo(legs));
    }
}