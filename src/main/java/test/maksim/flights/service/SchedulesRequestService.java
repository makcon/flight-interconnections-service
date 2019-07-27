package test.maksim.flights.service;

import test.maksim.flights.Config;
import test.maksim.flights.domain.FlightSchedule;
import test.maksim.flights.domain.ScheduleRequest;
import test.maksim.flights.domain.SchedulesResponse;
import test.maksim.flights.domain.SchedulesResponse.Day;
import test.maksim.flights.domain.SchedulesResponse.Schedule;
import test.maksim.flights.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulesRequestService {

    private static final String PATH_TEMPLATE = "/%s/%s/years/%d/months/%d";
    private static final int MINUTES_PER_DAY = 24 * 60;

    private final RestTemplate restTemplate;
    private final Config config;

    public List<FlightSchedule> request(ScheduleRequest request) {
        var fullUrl = config.getSchedulesServiceUrl() + String.format(
                PATH_TEMPLATE,
                request.getFrom(),
                request.getTo(),
                request.getDateTime().getYear(),
                request.getDateTime().getMonthValue()
        );

        try {
            log.info("Requesting schedules: {}", fullUrl);
            SchedulesResponse response = restTemplate.getForObject(fullUrl, SchedulesResponse.class);
            log.debug("Got response: {}", response);

            if (response == null) {
                log.warn("No schedules found");
                return emptyList();
            }

            List<Integer> range = IntStream.rangeClosed(
                    request.getDateTime().getDayOfMonth(),
                    request.getDateTimeEnd().getDayOfMonth()
            )
                    .boxed()
                    .collect(toList());

            return response.getDays().stream()
                    .filter(it -> range.contains(it.getDay()))
                    .flatMap(it -> buildFlightSchedules(it, request))
                    .filter(it -> it.getDepartureTime().isAfter(request.getDateTime()))
                    .filter(it -> DateTimeUtils.isBeforeOrEquals(it.getArrivalTime(), request.getDateTimeEnd()))
                    .collect(toList());
        } catch (Exception e) {
            log.error("Failed to request schedules: {}", request, e);
            return emptyList();
        }
    }

    private Stream<FlightSchedule> buildFlightSchedules(Day day,
                                                        ScheduleRequest request) {
        return day.getFlights().stream()
                .map(it -> buildFlightSchedule(day, request, it));
    }

    private FlightSchedule buildFlightSchedule(Day day,
                                               ScheduleRequest request,
                                               Schedule schedule) {
        var departureDate = LocalDate.of(
                request.getDateTime().getYear(),
                request.getDateTime().getMonthValue(),
                day.getDay()
        );
        var departureTime = LocalDateTime.of(departureDate, schedule.getDepartureTime());
        long flightDurationMin = calculateFlightDuration(schedule);

        return new FlightSchedule(departureTime, departureTime.plus(flightDurationMin, ChronoUnit.MINUTES));
    }

    private long calculateFlightDuration(Schedule schedule) {
        /*
            If arrival time is on next day, the service returns response like:
            {
                ...
                departureTime: "21:10",
                arrivalTime: "00:25"
            }
         */
        if (schedule.getDepartureTime().isAfter(schedule.getArrivalTime())) {
            return MINUTES_PER_DAY - Duration.between(schedule.getArrivalTime(), schedule.getDepartureTime()).toMinutes();
        }

        return Duration.between(schedule.getDepartureTime(), schedule.getArrivalTime()).toMinutes();
    }
}
