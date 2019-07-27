package test.maksim.flights.service;

import test.maksim.flights.Config;
import test.maksim.flights.domain.FlightSchedule;
import test.maksim.flights.domain.ScheduleRequest;
import test.maksim.flights.domain.SchedulesResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SchedulesRequestServiceTest {

    private static final int YEAR = 2019;
    private static final int MONTH = 7;
    private static final int DAY_1 = 1;
    private static final int DAY_2 = 2;
    private static final int DAY_3 = 3;
    private static final String SERVICE_URL = "url";
    private static final String AIRPORT_FROM = "a";
    private static final String AIRPORT_TO = "b";
    private static final LocalDateTime DATE_TIME = LocalDateTime.of(YEAR, MONTH, DAY_1, 9, 0);
    private static final LocalDateTime DATE_TIME_END = LocalDateTime.of(YEAR, MONTH, DAY_2, 7, 0);

    @InjectMocks
    private SchedulesRequestService service;

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Config config;

    @Before
    public void setUp() {
        mockConfig();
    }

    @Test
    public void request_emptyResponse_shouldReturnEmptyList() {
        mockSchedulesRequestService(createResponse(List.of()));

        List<FlightSchedule> flightSchedules = service.request(createRequest());

        assertThat(flightSchedules, hasSize(0));
    }

    @Test
    public void request_requestError_shouldReturnEmptyList() {
        mockSchedulesRequestServiceException();

        List<FlightSchedule> flightSchedules = service.request(createRequest());

        assertThat(flightSchedules, hasSize(0));
    }

    @Test
    public void request_3daysInSchedule_shouldMatch2Schedules() {
        var schedule1 = createSchedule(8, 9);
        var schedule2 = createSchedule(10, 12);
        var schedule3 = createSchedule(6, 7);
        var day1 = createDay(DAY_1, List.of(schedule1, schedule2));
        var day2 = createDay(DAY_2, List.of(schedule1, schedule3));
        var day3 = createDay(DAY_3, List.of(schedule1));
        var response = createResponse(List.of(day1, day2, day3));
        mockSchedulesRequestService(response);

        List<FlightSchedule> flightSchedules = service.request(createRequest());

        verifyFullUrl();
        var flightSchedule1 = createFlightSchedule(DAY_1, 10, DAY_1, 12);
        var flightSchedule2 = createFlightSchedule(DAY_2, 6, DAY_2, 7);
        assertThat(flightSchedules, equalTo(List.of(flightSchedule1, flightSchedule2)));
    }

    @Test
    public void request_arrivalTimeIsOnNextDay_shouldBuildProperDate() {
        var schedule1 = createSchedule(23, 1);
        var day1 = createDay(DAY_1, List.of(schedule1));
        var response = createResponse(List.of(day1));
        mockSchedulesRequestService(response);

        List<FlightSchedule> flightSchedules = service.request(createRequest());

        verifyFullUrl();
        var flightSchedule1 = createFlightSchedule(DAY_1, 23, DAY_2, 1);
        assertThat(flightSchedules, equalTo(List.of(flightSchedule1)));
    }

    // Util methods

    private ScheduleRequest createRequest() {
        return ScheduleRequest.builder()
                .from(AIRPORT_FROM)
                .to(AIRPORT_TO)
                .dateTime(DATE_TIME)
                .dateTimeEnd(DATE_TIME_END)
                .build();
    }

    private SchedulesResponse createResponse(List<SchedulesResponse.Day> days) {
        var response = new SchedulesResponse();
        response.setMonth(MONTH);
        response.setDays(days);

        return response;
    }

    private SchedulesResponse.Day createDay(int dayN,
                                            List<SchedulesResponse.Schedule> schedules) {
        var day = new SchedulesResponse.Day();
        day.setDay(dayN);
        day.setFlights(schedules);

        return day;
    }

    private SchedulesResponse.Schedule createSchedule(int departureHour,
                                                      int arrivalHour) {
        var schedule = new SchedulesResponse.Schedule();
        schedule.setDepartureTime(LocalTime.of(departureHour, 0));
        schedule.setArrivalTime(LocalTime.of(arrivalHour, 0));

        return schedule;
    }

    private FlightSchedule createFlightSchedule(int departureDay,
                                                int departureHour,
                                                int arrivalDay,
                                                int arrivalHour) {
        var departure = LocalDateTime.of(YEAR, MONTH, departureDay, departureHour, 0);
        var arrival = LocalDateTime.of(YEAR, MONTH, arrivalDay, arrivalHour, 0);

        return new FlightSchedule(departure, arrival);
    }

    private void mockSchedulesRequestService(SchedulesResponse response) {
        when(restTemplate.getForObject(anyString(), eq(SchedulesResponse.class))).thenReturn(response);
    }

    private void mockSchedulesRequestServiceException() {
        doThrow(RuntimeException.class).when(restTemplate).getForObject(anyString(), eq(SchedulesResponse.class));
    }

    private void mockConfig() {
        when(config.getSchedulesServiceUrl()).thenReturn(SERVICE_URL);
    }

    private void verifyFullUrl() {
        var fullUrl = SERVICE_URL + String.format("/%s/%s/years/%d/months/%d", AIRPORT_FROM, AIRPORT_TO, YEAR, MONTH);
        verify(restTemplate).getForObject(eq(fullUrl), eq(SchedulesResponse.class));
    }
}