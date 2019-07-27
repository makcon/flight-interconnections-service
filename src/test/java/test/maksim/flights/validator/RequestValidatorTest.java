package test.maksim.flights.validator;

import test.maksim.flights.domain.FlightsRequest;
import org.junit.Test;

import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.MINUTES;

public class RequestValidatorTest {

    private final RequestValidator validator = new RequestValidator();

    @Test
    public void validate_validRequest_noException() {
        var request = createValidBuilder().build();

        validator.validate(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_maxStopsNegative_shouldThrowException() {
        var request = createValidBuilder().maxStops(-1).build();

        validator.validate(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_maxStopsMoreThan1_shouldThrowException() {
        var request = createValidBuilder().maxStops(2).build();

        validator.validate(request);
    }

    @Test
    public void validate_maxStops0_noException() {
        var request = createValidBuilder().maxStops(0).build();

        validator.validate(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_arrivalDateLaterBeforeDeparture_shouldThrowException() {
        var request = createValidBuilder()
                .arrivalDateTime(LocalDateTime.parse("2019-07-01T06:00"))
                .build();

        validator.validate(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_arrivalDateLaterBeforeEqualsDeparture_shouldThrowException() {
        var request = createValidBuilder()
                .arrivalDateTime(LocalDateTime.parse("2019-07-01T07:00"))
                .build();

        validator.validate(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_departureDateBeforeNow_shouldThrowException() {
        var request = createValidBuilder()
                .arrivalDateTime(LocalDateTime.now().minus(1, MINUTES))
                .build();

        validator.validate(request);
    }

    // Util methods

    private FlightsRequest.FlightsRequestBuilder createValidBuilder() {
        return FlightsRequest.builder()
                .maxStops(1)
                .departureDateTime(LocalDateTime.parse("2149-07-01T07:00"))
                .arrivalDateTime(LocalDateTime.parse("2149-07-01T08:00"));
    }
}