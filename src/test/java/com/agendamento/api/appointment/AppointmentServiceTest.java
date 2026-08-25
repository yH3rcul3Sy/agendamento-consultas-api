package com.agendamento.api.appointment;

import com.agendamento.api.appointment.dto.AppointmentRequest;
import com.agendamento.api.appointment.dto.AppointmentResponse;
import com.agendamento.api.exception.BusinessException;
import com.agendamento.api.exception.ResourceNotFoundException;
import com.agendamento.api.notification.AppointmentNotificationService;
import com.agendamento.api.patient.Patient;
import com.agendamento.api.patient.PatientRepository;
import com.agendamento.api.professional.Professional;
import com.agendamento.api.professional.ProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private ProfessionalRepository professionalRepository;
    @Mock
    private AppointmentNotificationService notificationService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Professional professional;

    // 15/01/2024 e uma segunda-feira; 13/01/2024 e um sabado.
    private static final LocalDateTime SEGUNDA_10H = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final LocalDateTime SABADO_10H = LocalDateTime.of(2024, 1, 13, 10, 0);

    @BeforeEach
    void setUp() {
        patient = Patient.builder().id(1L).name("Joao").email("joao@teste.com").build();
        professional = Professional.builder()
                .id(1L).name("Dr. Carlos").specialty("Clinico Geral").email("carlos@clinica.com")
                .workStart(LocalTime.of(8, 0)).workEnd(LocalTime.of(18, 0))
                .build();
    }

    @Test
    void deveCriarConsultaDentroDoExpedienteEmDiaUtil() {
        AppointmentRequest request = new AppointmentRequest(1L, 1L, SEGUNDA_10H, 30, null);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(appointmentRepository.findByProfessionalIdAndStatusAndDateTimeBetween(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.create(request);

        assertThat(response.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(response.durationMinutes()).isEqualTo(30);
        verify(notificationService).notifyConfirmation(any());
    }

    @Test
    void naoDeveCriarConsultaNoFimDeSemana() {
        AppointmentRequest request = new AppointmentRequest(1L, 1L, SABADO_10H, 30, null);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("dias uteis");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarConsultaForaDoExpedienteDoProfissional() {
        LocalDateTime foraDoExpediente = LocalDateTime.of(2024, 1, 15, 20, 0); // 20h, expediente vai ate 18h
        AppointmentRequest request = new AppointmentRequest(1L, 1L, foraDoExpediente, 30, null);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("expediente");
    }

    @Test
    void naoDeveCriarConsultaComConflitoDeHorario() {
        AppointmentRequest request = new AppointmentRequest(1L, 1L, SEGUNDA_10H, 30, null);

        Appointment existente = Appointment.builder()
                .id(5L).patient(patient).professional(professional)
                .dateTime(SEGUNDA_10H).durationMinutes(30).status(AppointmentStatus.SCHEDULED)
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(1L)).thenReturn(Optional.of(professional));
        when(appointmentRepository.findByProfessionalIdAndStatusAndDateTimeBetween(any(), any(), any(), any()))
                .thenReturn(List.of(existente));

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ja existe uma consulta");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarConsultaParaPacienteInexistente() {
        AppointmentRequest request = new AppointmentRequest(99L, 1L, SEGUNDA_10H, 30, null);
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void naoDeveCriarConsultaParaProfissionalInexistente() {
        AppointmentRequest request = new AppointmentRequest(1L, 99L, SEGUNDA_10H, 30, null);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(professionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveCancelarConsultaAgendada() {
        Appointment appointment = Appointment.builder()
                .id(1L).patient(patient).professional(professional)
                .dateTime(SEGUNDA_10H).durationMinutes(30).status(AppointmentStatus.SCHEDULED)
                .build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.cancel(1L);

        assertThat(response.status()).isEqualTo(AppointmentStatus.CANCELED);
        verify(notificationService).notifyCancellation(any());
    }

    @Test
    void naoDeveCancelarConsultaJaCancelada() {
        Appointment appointment = Appointment.builder()
                .id(1L).patient(patient).professional(professional)
                .dateTime(SEGUNDA_10H).durationMinutes(30).status(AppointmentStatus.CANCELED)
                .build();
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancel(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ja esta cancelada");

        verify(notificationService, never()).notifyCancellation(any());
    }
}
