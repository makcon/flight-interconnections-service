package test.maksim.flights.service;

import test.maksim.flights.Config;
import test.maksim.flights.domain.Route;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutesRequestService {

    private final RestTemplate restTemplate;
    private final Config config;

    public List<Route> request(String connectingAirport,
                               Collection<String> operators) {
        log.info("Requesting routes: {}", config.getRoutesServiceUrl());
        Route[] routeList = restTemplate.getForObject(config.getRoutesServiceUrl(), Route[].class);

        if (routeList == null) {
            log.warn("No routes found");
            return emptyList();
        }

        return Stream.of(routeList)
                .filter(it -> Objects.equals(connectingAirport, it.getConnectingAirport()))
                .filter(it -> operators.contains(it.getOperator()))
                .collect(toList());
    }
}
