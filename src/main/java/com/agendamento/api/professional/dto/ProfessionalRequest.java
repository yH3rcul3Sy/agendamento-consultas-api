package com.agendamento.api.professional.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ProfessionalRequest(
        @NotBlank(message = "Nome e obrigatorio") String name,

        @NotBlank(message = "Especialidade e obrigatoria") String specialty,

        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido") String email,

        @NotNull(message = "Horario de inicio do expediente e obrigatorio (formato HH:mm)") LocalTime workStart,

        @NotNull(message = "Horario de fim do expediente e obrigatorio (formato HH:mm)") LocalTime workEnd
) {
}
