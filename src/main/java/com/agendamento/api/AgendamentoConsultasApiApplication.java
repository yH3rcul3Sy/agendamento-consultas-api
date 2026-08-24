package com.agendamento.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AgendamentoConsultasApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgendamentoConsultasApiApplication.class, args);
	}

}
