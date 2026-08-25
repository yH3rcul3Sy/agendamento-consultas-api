package com.agendamento.api.professional;

import com.agendamento.api.exception.BusinessException;
import com.agendamento.api.exception.ResourceNotFoundException;
import com.agendamento.api.professional.dto.ProfessionalRequest;
import com.agendamento.api.professional.dto.ProfessionalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @InjectMocks
    private ProfessionalService professionalService;

    private Professional professional;

    @BeforeEach
    void setUp() {
        professional = Professional.builder()
                .id(1L).name("Dr. Carlos").specialty("Clinico Geral").email("carlos@clinica.com")
                .workStart(LocalTime.of(8, 0)).workEnd(LocalTime.of(18, 0))
                .build();
    }

    @Test
    void deveCriarProfissionalComExpedienteValido() {
        ProfessionalRequest request = new ProfessionalRequest(
                "Dr. Carlos", "Clinico Geral", "carlos@clinica.com", LocalTime.of(8, 0), LocalTime.of(18, 0));
        when(professionalRepository.existsByEmail("carlos@clinica.com")).thenReturn(false);
        when(professionalRepository.save(any(Professional.class))).thenReturn(professional);

        ProfessionalResponse response = professionalService.create(request);

        assertThat(response.workStart()).isEqualTo(LocalTime.of(8, 0));
    }

    @Test
    void naoDeveCriarProfissionalComExpedienteInvalido() {
        // fim do expediente antes do inicio
        ProfessionalRequest request = new ProfessionalRequest(
                "Dr. Carlos", "Clinico Geral", "carlos@clinica.com", LocalTime.of(18, 0), LocalTime.of(8, 0));

        assertThatThrownBy(() -> professionalService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("depois do horario de inicio");

        verify(professionalRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarProfissionalComEmailDuplicado() {
        ProfessionalRequest request = new ProfessionalRequest(
                "Dr. Carlos", "Clinico Geral", "carlos@clinica.com", LocalTime.of(8, 0), LocalTime.of(18, 0));
        when(professionalRepository.existsByEmail("carlos@clinica.com")).thenReturn(true);

        assertThatThrownBy(() -> professionalService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");
    }

    @Test
    void deveLancarExcecaoAoBuscarProfissionalInexistente() {
        when(professionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> professionalService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
