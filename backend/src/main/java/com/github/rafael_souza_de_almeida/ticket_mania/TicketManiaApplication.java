package com.github.rafael_souza_de_almeida.ticket_mania;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableScheduling
@CrossOrigin(
		origins = "http://localhost:3000",
		allowCredentials = "true"
)
public class TicketManiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketManiaApplication.class, args);
	}

}
