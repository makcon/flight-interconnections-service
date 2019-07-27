package test.maksim.flights.builder;

import test.maksim.flights.Config;
import test.maksim.flights.rest.dto.Flight;
import test.maksim.flights.domain.FlightSchedule;
import test.maksim.flights.domain.FlightsRequest;
import test.maksim.flights.domain.Route;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import test.maksim.flights.TestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FlightsBuilderTest {

    private static final int MIN_STOP_DURATION = 2;
    private static final String DEFAULT_DEPARTURE_TIME = "2019-07-01T01:00";
    private static final String DEFAULT_ARRIVAL_TIME = "2019-07-02T01:00";

    @InjectMocks
    private FlightsBuilder builder;

    @Mock
    private Config config;

    private final Route route = TestUtils.createRoute("a", "b");

    @Test
    public void createFlights_1Of3flightsOutOfDate_shouldCreate2Flights() {
        var arrivalTime1 = "2019-07-01T09:00";
        var arrivalTime2 = "2019-07-01T10:00";
        var arrivalTime3 = "2019-07-01T08:00";

        List<Flight> flights = builder.createFlights(
                createFlightsRequest("2019-07-01T09:00"),
                1,
                route,
                List.of(createFlightSchedule(arrivalTime1), createFlightSchedule(arrivalTime2), createFlightSchedule(arrivalTime3))
        );

        List<Flight> expectedFlights = List.of(
                createSingleLegFlight(1, arrivalTime1),
                createSingleLegFlight(1, arrivalTime3)
        );
        assertThat(flights, equalTo(expectedFlights));
    }

    @Test
    public void addAvailableLegs_4Flights1OutOfRange_shouldConnect3() {
        mockConfig();
        var leg1ArrivalTime1 = "2019-07-01T09:00";
        var leg1ArrivalTime2 = "2019-07-01T10:00";
        var leg1ArrivalTime3 = "2019-07-01T11:00";
        var leg1ArrivalTime4 = "2019-07-01T15:00";
        var flight1 = createSingleLegFlight(1, leg1ArrivalTime1);
        var flight2 = createSingleLegFlight(1, leg1ArrivalTime2);
        var flight3 = createSingleLegFlight(1, leg1ArrivalTime3);
        var flight4 = createSingleLegFlight(1, leg1ArrivalTime4);
        var leg2ArrivalTime1 = "2019-07-01T11:00";
        var leg2ArrivalTime2 = "2019-07-01T13:01";
        List<FlightSchedule> flightSchedules = List.of(
                createFlightScheduleDeparture(leg2ArrivalTime1),
                createFlightScheduleDeparture(leg2ArrivalTime2)
        );

        builder.addAvailableLegs(
                route,
                flightSchedules,
                List.of(flight1, flight2, flight3)
        );

        verifyLeg2(flight1, leg2ArrivalTime1);
        verifyLeg2(flight2, leg2ArrivalTime2);
        verifyLeg2(flight3, leg2ArrivalTime2);
        assertThat(flight4.getLegs(), hasSize(1));
    }

    // Util methods

    private FlightsRequest createFlightsRequest(String arrivalTime) {
        return FlightsRequest.builder()
                .arrivalDateTime(LocalDateTime.parse(arrivalTime))
                .build();
    }

    private FlightSchedule createFlightSchedule(String arrivalTime) {
        return new FlightSchedule(
                LocalDateTime.parse(DEFAULT_DEPARTURE_TIME),
                LocalDateTime.parse(arrivalTime)
        );
    }

    private FlightSchedule createFlightScheduleDeparture(String departureTime) {
        return new FlightSchedule(
                LocalDateTime.parse(departureTime),
                LocalDateTime.parse(DEFAULT_ARRIVAL_TIME)
        );
    }

    private Flight createSingleLegFlight(int stops,
                                         String arrivalTime) {
        List<Flight.Leg> legs = new ArrayList<>();
        legs.add(createLeg(DEFAULT_DEPARTURE_TIME, arrivalTime));

        return new Flight(stops, legs);
    }

    private Flight.Leg createLeg(String departureTime,
                                 String arrivalTime) {
        return Flight.Leg.builder()
                    .departureAirport(route.getAirportFrom())
                    .arrivalAirport(route.getAirportTo())
                    .departureDateTime(LocalDateTime.parse(departureTime))
                    .arrivalDateTime(LocalDateTime.parse(arrivalTime))
                    .build();
    }

    private void mockConfig() {
        when(config.getMinStopDuration()).thenReturn(MIN_STOP_DURATION);
    }

    private void verifyLeg2(Flight flight,
                            String arrivalTime) {
        var leg2 = createLeg(arrivalTime, DEFAULT_ARRIVAL_TIME);
        assertThat(flight.getLegs(), hasSize(2));
        assertThat(flight.getLegs().get(1), equalTo(leg2));
    }
}