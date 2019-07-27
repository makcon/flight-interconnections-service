package test.maksim.flights.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FlightSchedule {

    private final LocalDateTime departureTime;
    private final LocalDateTime arrivalTime;
}
