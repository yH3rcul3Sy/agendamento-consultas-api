package com.agendamento.api.appointment.dto;

import com.agendamento.api.appointment.Appointment;
import com.agendamento.api.appointment.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        Long professionalId,
        String professionalName,
        LocalDateTime dateTime,
        Integer durationMinutes,
        AppointmentStatus status,
        String notes
) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getProfessional().getId(),
                appointment.getProfessional().getName(),
                appointment.getDateTime(),
                appointment.getDurationMinutes(),
                appointment.getStatus(),
                appointment.getNotes()
        );
    }
}
