package test.maksim.flights.domain;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class SchedulesResponse {

    private int month;
    private List<Day> days;

    @Data
    public static class Day {

        private int day;
        private List<Schedule> flights;
    }

    @Data
    public static class Schedule {

        private int number;
        private LocalTime departureTime;
        private LocalTime arrivalTime;
    }
}
