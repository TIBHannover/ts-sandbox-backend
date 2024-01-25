package eu.tib.tiva;

import eu.tib.tiva.configuration.TivaProcessingConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties(value = TivaProcessingConfig.class)
public class TivaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TivaApplication.class, args);
		log.info("tiva application started");
	}
}
