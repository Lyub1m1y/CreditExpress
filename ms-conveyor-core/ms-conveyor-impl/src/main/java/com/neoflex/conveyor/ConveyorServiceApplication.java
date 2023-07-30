package com.neoflex.conveyor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ConveyorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConveyorServiceApplication.class, args);
	}

}
