package tn.beecoders.elderly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ElderlyAssistanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElderlyAssistanceApplication.class, args);
	}

}
