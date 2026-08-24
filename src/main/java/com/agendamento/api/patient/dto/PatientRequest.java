package com.agendamento.api.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PatientRequest(
        @NotBlank(message = "Nome e obrigatorio") String name,

        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido") String email,

        String phone
) {
}
