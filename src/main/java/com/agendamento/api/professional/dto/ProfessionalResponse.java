package com.agendamento.api.professional.dto;

import com.agendamento.api.professional.Professional;

import java.time.LocalTime;

public record ProfessionalResponse(
        Long id,
        String name,
        String specialty,
        String email,
        LocalTime workStart,
        LocalTime workEnd
) {
    public static ProfessionalResponse from(Professional professional) {
        return new ProfessionalResponse(
                professional.getId(),
                professional.getName(),
                professional.getSpecialty(),
                professional.getEmail(),
                professional.getWorkStart(),
                professional.getWorkEnd()
        );
    }
}
