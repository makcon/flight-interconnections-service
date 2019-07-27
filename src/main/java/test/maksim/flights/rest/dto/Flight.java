package test.maksim.flights.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Flight {

    private final int stops;
    private final List<Leg> legs;

    @Data
    @Builder
    public static class Leg {

        private final String departureAirport;
        private final String arrivalAirport;
        private final LocalDateTime departureDateTime;
        private final LocalDateTime arrivalDateTime;
    }
}
