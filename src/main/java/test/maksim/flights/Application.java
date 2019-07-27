package test.maksim.flights;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public AsyncListenableTaskExecutor serviceExecutor(@Value("${service-executor.core.pool.size:10}") int corePoolSize,
                                                       @Value("${service-executor.max.pool.size:15}") int maxPoolSize) {
        var executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("flight-ws-");
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);

        return executor;
    }

    @Bean
    public AsyncTaskExecutor schedulesExecutor(@Value("${schedules-executor.core.pool.size:30}") int corePoolSize,
                                               @Value("${schedules-executor.max.pool.size:40}") int maxPoolSize) {
        var executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("schedules-");
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);

        return executor;
    }
}
