package com.agendamento.api.patient;

import com.agendamento.api.exception.BusinessException;
import com.agendamento.api.exception.ResourceNotFoundException;
import com.agendamento.api.patient.dto.PatientRequest;
import com.agendamento.api.patient.dto.PatientResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = Patient.builder().id(1L).name("Joao").email("joao@teste.com").phone("11999999999").build();
    }

    @Test
    void deveCriarPacienteQuandoEmailNaoExiste() {
        PatientRequest request = new PatientRequest("Joao", "joao@teste.com", "11999999999");
        when(patientRepository.existsByEmail("joao@teste.com")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        PatientResponse response = patientService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void naoDeveCriarPacienteComEmailDuplicado() {
        PatientRequest request = new PatientRequest("Joao", "joao@teste.com", "11999999999");
        when(patientRepository.existsByEmail("joao@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> patientService.create(request))
                .isInstanceOf(BusinessException.class);

        verify(patientRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoBuscarPacienteInexistente() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> patientService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveListarTodosOsPacientes() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        List<PatientResponse> result = patientService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Joao");
    }

    @Test
    void deveRemoverPacienteExistente() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        patientService.delete(1L);

        verify(patientRepository).delete(patient);
    }
}
