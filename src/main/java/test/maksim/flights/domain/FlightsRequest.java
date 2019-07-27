package test.maksim.flights.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FlightsRequest {

    private final String departureAirport;
    private final String arrivalAirport;
    private final LocalDateTime departureDateTime;
    private final LocalDateTime arrivalDateTime;
    private final int maxStops;
}
