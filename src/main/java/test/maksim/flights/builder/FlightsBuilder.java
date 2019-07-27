package test.maksim.flights.builder;

import test.maksim.flights.Config;
import test.maksim.flights.domain.FlightSchedule;
import test.maksim.flights.domain.FlightsRequest;
import test.maksim.flights.domain.Route;
import test.maksim.flights.rest.dto.Flight;
import test.maksim.flights.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@Component
@RequiredArgsConstructor
@Slf4j
public class FlightsBuilder {

    private final Config config;

    public List<Flight> createFlights(FlightsRequest request,
                                      int stops,
                                      Route route,
                                      List<FlightSchedule> flightSchedules) {
       return flightSchedules.stream()
                .filter(it -> DateTimeUtils.isBeforeOrEquals(it.getArrivalTime(), request.getArrivalDateTime()))
                .map(it -> createFlight(it, route, stops))
                .collect(Collectors.toList());
    }

    public void addAvailableLegs(Route route,
                                 List<FlightSchedule> flightSchedules,
                                 List<Flight> flights) {
        for (var flightSchedule : flightSchedules) {
            for (var flight : flights) {
                if (flight.getLegs().size() - 1 == flight.getStops()) { // skip if already found
                    continue;
                }

                var lastLeg = flight.getLegs().get(flight.getLegs().size() - 1);
                LocalDateTime nextTime = lastLeg.getArrivalDateTime().plus(config.getMinStopDuration(), HOURS);
                if (DateTimeUtils.isBeforeOrEquals(nextTime, flightSchedule.getDepartureTime())) {
                    flight.getLegs().add(createLeg(route, flightSchedule));
                }
            }
        }
    }

    private Flight createFlight(FlightSchedule flightSchedule,
                                Route route,
                                int stops) {
        List<Flight.Leg> legs = new ArrayList<>();
        legs.add(createLeg(route, flightSchedule));

        return new Flight(stops, legs);
    }

    private Flight.Leg createLeg(Route route,
                                 FlightSchedule flightSchedule) {
        return Flight.Leg.builder()
                .departureAirport(route.getAirportFrom())
                .arrivalAirport(route.getAirportTo())
                .departureDateTime(flightSchedule.getDepartureTime())
                .arrivalDateTime(flightSchedule.getArrivalTime())
                .build();
    }
}
