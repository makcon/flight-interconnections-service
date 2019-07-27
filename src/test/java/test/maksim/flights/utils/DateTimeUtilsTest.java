package test.maksim.flights.utils;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DateTimeUtilsTest {

    @Test
    public void isBeforeOrEquals_time1BeforeTime2_shouldBeTrue() {
        var time1 = LocalDateTime.now();
        var time2 = time1.plus(1, ChronoUnit.SECONDS);

        boolean result = DateTimeUtils.isBeforeOrEquals(time1, time2);

        assertThat(result, is(true));
    }

    @Test
    public void isBeforeOrEquals_time1EqualsTime2_shouldBeTrue() {
        var time1 = LocalDateTime.now();

        boolean result = DateTimeUtils.isBeforeOrEquals(time1, time1);

        assertThat(result, is(true));
    }

    @Test
    public void isBeforeOrEquals_time1AfterTime2_shouldBeFalse() {
        var time1 = LocalDateTime.now();
        var time2 = time1.minus(1, ChronoUnit.SECONDS);

        boolean result = DateTimeUtils.isBeforeOrEquals(time1, time2);

        assertThat(result, is(false));
    }
}