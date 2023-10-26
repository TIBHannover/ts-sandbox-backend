package eu.tib.ts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TerminologyServiceStatisticsApplication {

    public static void main(String[] args) {
        log.info("starting main upar");
        SpringApplication.run(TerminologyServiceStatisticsApplication.class, args);
        log.info("starting main");
        log.error("starting main");
    }

}
