package com.agendamento.api.patient;

import com.agendamento.api.exception.BusinessException;
import com.agendamento.api.exception.ResourceNotFoundException;
import com.agendamento.api.patient.dto.PatientRequest;
import com.agendamento.api.patient.dto.PatientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public List<PatientResponse> findAll() {
        return patientRepository.findAll().stream().map(PatientResponse::from).toList();
    }

    public PatientResponse findById(Long id) {
        return PatientResponse.from(getPatientOrThrow(id));
    }

    public PatientResponse create(PatientRequest request) {
        if (patientRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ja existe um paciente cadastrado com este email");
        }
        Patient patient = Patient.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .build();
        return PatientResponse.from(patientRepository.save(patient));
    }

    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = getPatientOrThrow(id);
        patient.setName(request.name());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        return PatientResponse.from(patientRepository.save(patient));
    }

    public void delete(Long id) {
        patientRepository.delete(getPatientOrThrow(id));
    }

    Patient getPatientOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente nao encontrado com id: " + id));
    }
}
