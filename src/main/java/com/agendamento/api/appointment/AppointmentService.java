package com.agendamento.api.appointment;

import com.agendamento.api.appointment.dto.AppointmentRequest;
import com.agendamento.api.appointment.dto.AppointmentResponse;
import com.agendamento.api.exception.BusinessException;
import com.agendamento.api.exception.ResourceNotFoundException;
import com.agendamento.api.notification.AppointmentEmailData;
import com.agendamento.api.notification.AppointmentNotificationService;
import com.agendamento.api.patient.Patient;
import com.agendamento.api.patient.PatientRepository;
import com.agendamento.api.professional.Professional;
import com.agendamento.api.professional.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProfessionalRepository professionalRepository;
    private final AppointmentNotificationService notificationService;

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAll() {
        return appointmentRepository.findAll().stream().map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(Long id) {
        return AppointmentResponse.from(getAppointmentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream().map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findByProfessional(Long professionalId) {
        return appointmentRepository.findByProfessionalId(professionalId).stream().map(AppointmentResponse::from).toList();
    }

    /**
     * Cria um agendamento validando: dia util, dentro do expediente do
     * profissional e sem conflito com outra consulta ja marcada.
     */
    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente nao encontrado com id: " + request.patientId()));

        Professional professional = professionalRepository.findById(request.professionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional nao encontrado com id: " + request.professionalId()));

        Integer duration = request.durationMinutesOrDefault();
        LocalDateTime start = request.dateTime();
        LocalDateTime end = start.plusMinutes(duration);

        validateBusinessDay(start);
        validateWorkingHours(professional, start, end);
        validateNoConflict(professional.getId(), start, end);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .professional(professional)
                .dateTime(start)
                .durationMinutes(duration)
                .status(AppointmentStatus.SCHEDULED)
                .notes(request.notes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        notificationService.notifyConfirmation(toEmailData(saved));

        return AppointmentResponse.from(saved);
    }

    @Transactional
    public AppointmentResponse cancel(Long id) {
        Appointment appointment = getAppointmentOrThrow(id);

        if (appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new BusinessException("Esta consulta ja esta cancelada");
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        Appointment saved = appointmentRepository.save(appointment);
        notificationService.notifyCancellation(toEmailData(saved));

        return AppointmentResponse.from(saved);
    }

    /**
     * Extrai os dados necessarios para o e-mail ainda dentro da transacao
     * (thread e sessao originais), antes de disparar o envio assincrono.
     */
    private AppointmentEmailData toEmailData(Appointment appointment) {
        return new AppointmentEmailData(
                appointment.getPatient().getName(),
                appointment.getPatient().getEmail(),
                appointment.getProfessional().getName(),
                appointment.getProfessional().getSpecialty(),
                appointment.getDateTime(),
                appointment.getDurationMinutes()
        );
    }

    private void validateBusinessDay(LocalDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new BusinessException("Consultas so podem ser marcadas em dias uteis (segunda a sexta)");
        }
    }

    private void validateWorkingHours(Professional professional, LocalDateTime start, LocalDateTime end) {
        LocalTime startTime = start.toLocalTime();
        LocalTime endTime = end.toLocalTime();

        if (startTime.isBefore(professional.getWorkStart()) || endTime.isAfter(professional.getWorkEnd())) {
            throw new BusinessException(
                    "Horario fora do expediente do profissional (%s - %s)"
                            .formatted(professional.getWorkStart(), professional.getWorkEnd())
            );
        }
    }

    private void validateNoConflict(Long professionalId, LocalDateTime start, LocalDateTime end) {
        LocalDateTime dayStart = start.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<Appointment> sameDayAppointments = appointmentRepository
                .findByProfessionalIdAndStatusAndDateTimeBetween(professionalId, AppointmentStatus.SCHEDULED, dayStart, dayEnd);

        boolean hasConflict = sameDayAppointments.stream().anyMatch(existing ->
                start.isBefore(existing.getEndDateTime()) && existing.getDateTime().isBefore(end)
        );

        if (hasConflict) {
            throw new BusinessException("Ja existe uma consulta marcada para este profissional neste horario");
        }
    }

    Appointment getAppointmentOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta nao encontrada com id: " + id));
    }
}
