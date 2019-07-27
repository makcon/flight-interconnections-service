package test.maksim.flights.utils;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.Objects;

@UtilityClass
public class DateTimeUtils {

    public boolean isBeforeOrEquals(LocalDateTime time1,
                                    LocalDateTime time2) {
        return time1.isBefore(time2) || Objects.equals(time1, time2);
    }
}
