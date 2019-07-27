package test.maksim.flights.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ScheduleRequest {

    private final String from;
    private final String to;
    private final LocalDateTime dateTime;
    private final LocalDateTime dateTimeEnd;
}
