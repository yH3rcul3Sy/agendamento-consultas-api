package com.agendamento.api.appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByProfessionalId(Long professionalId);

    /** Usado para checar conflito de horario: pega os agendamentos ativos do profissional no dia. */
    List<Appointment> findByProfessionalIdAndStatusAndDateTimeBetween(
            Long professionalId, AppointmentStatus status, LocalDateTime start, LocalDateTime end
    );
}
