package com.agendamento.api.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull(message = "Id do paciente e obrigatorio") Long patientId,

        @NotNull(message = "Id do profissional e obrigatorio") Long professionalId,

        @NotNull(message = "Data e horario sao obrigatorios")
        @Future(message = "A consulta deve ser marcada para uma data futura") LocalDateTime dateTime,

        @Positive(message = "Duracao deve ser maior que zero") Integer durationMinutes,

        String notes
) {
    /** Duracao padrao de 30 minutos quando nao informada. */
    public Integer durationMinutesOrDefault() {
        return durationMinutes != null ? durationMinutes : 30;
    }
}
