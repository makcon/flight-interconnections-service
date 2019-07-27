package test.maksim.flights;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class Config {

    private final int minStopDuration;
    private final String routesServiceUrl;
    private final String schedulesServiceUrl;
    private final int defaultMaxStops;

    public Config(@Value("${min.stop.duration.hour:2}") int minStopDuration,
                  @Value("${routes.service.url:https://services-api.ryanair.com/locate/3/routes}") String routesServiceUrl,
                  @Value("${schedules.service.url:https://services-api.ryanair.com/timtbl/3/schedules}") String schedulesServiceUrl,
                  @Value("${default.max.stops:1}") int defaultMaxStops) {
        this.minStopDuration = minStopDuration;
        this.routesServiceUrl = routesServiceUrl;
        this.schedulesServiceUrl = schedulesServiceUrl;
        this.defaultMaxStops = defaultMaxStops;
    }
}
