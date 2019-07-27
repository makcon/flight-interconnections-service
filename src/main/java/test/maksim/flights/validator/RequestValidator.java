package test.maksim.flights.validator;

import test.maksim.flights.domain.FlightsRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RequestValidator {

    public void validate(FlightsRequest request) {
        validateMaxStops(request);
        validateDates(request);

        // TODO add more rules
    }

    private void validateMaxStops(FlightsRequest request) {
        if (request.getMaxStops() < 0) {
            throw new IllegalArgumentException("Max stops cannot be negative");
        }
        if (request.getMaxStops() > 1) {
            throw new IllegalArgumentException("Max stops > 1 is not supported yet");
        }
    }

    private void validateDates(FlightsRequest request) {
        if (request.getDepartureDateTime().isAfter(request.getArrivalDateTime())) {
            throw new IllegalArgumentException("Departure time must not be later than arrival time");
        }

        if (request.getDepartureDateTime().equals(request.getArrivalDateTime())) {
            throw new IllegalArgumentException("Departure and arrival times cannot be equal");
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.getDepartureDateTime().isBefore(now)) {
            throw new IllegalArgumentException("Departure time must be later than now: " + now);
        }
    }
}
