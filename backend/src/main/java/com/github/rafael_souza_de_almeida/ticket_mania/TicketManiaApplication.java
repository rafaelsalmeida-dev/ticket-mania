package com.github.rafael_souza_de_almeida.ticket_mania;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketManiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketManiaApplication.class, args);
	}

}
