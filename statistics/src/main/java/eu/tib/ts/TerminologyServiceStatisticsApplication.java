package eu.tib.ts;

import eu.tib.ts.configuration.OntologiesProcessingConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties(value = OntologiesProcessingConfig.class)
public class TerminologyServiceStatisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TerminologyServiceStatisticsApplication.class, args);
    }

}
