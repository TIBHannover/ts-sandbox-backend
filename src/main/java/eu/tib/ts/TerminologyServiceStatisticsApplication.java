package eu.tib.ts;

import org.jfree.util.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableAutoConfiguration
public class TerminologyServiceStatisticsApplication {

    public static void main(String[] args) {
        Log.info("starting main upar");
        SpringApplication.run(TerminologyServiceStatisticsApplication.class, args);
        Log.info("starting main");
        System.out.println("starting main");
    }

}
