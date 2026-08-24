package com.agendamento.api.professional;

import com.agendamento.api.exception.BusinessException;
import com.agendamento.api.exception.ResourceNotFoundException;
import com.agendamento.api.professional.dto.ProfessionalRequest;
import com.agendamento.api.professional.dto.ProfessionalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;

    public List<ProfessionalResponse> findAll() {
        return professionalRepository.findAll().stream().map(ProfessionalResponse::from).toList();
    }

    public ProfessionalResponse findById(Long id) {
        return ProfessionalResponse.from(getProfessionalOrThrow(id));
    }

    public ProfessionalResponse create(ProfessionalRequest request) {
        validateWorkHours(request);
        if (professionalRepository.existsByEmail(request.email())) {
            throw new BusinessException("Ja existe um profissional cadastrado com este email");
        }
        Professional professional = Professional.builder()
                .name(request.name())
                .specialty(request.specialty())
                .email(request.email())
                .workStart(request.workStart())
                .workEnd(request.workEnd())
                .build();
        return ProfessionalResponse.from(professionalRepository.save(professional));
    }

    public ProfessionalResponse update(Long id, ProfessionalRequest request) {
        validateWorkHours(request);
        Professional professional = getProfessionalOrThrow(id);
        professional.setName(request.name());
        professional.setSpecialty(request.specialty());
        professional.setEmail(request.email());
        professional.setWorkStart(request.workStart());
        professional.setWorkEnd(request.workEnd());
        return ProfessionalResponse.from(professionalRepository.save(professional));
    }

    public void delete(Long id) {
        professionalRepository.delete(getProfessionalOrThrow(id));
    }

    Professional getProfessionalOrThrow(Long id) {
        return professionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional nao encontrado com id: " + id));
    }

    private void validateWorkHours(ProfessionalRequest request) {
        if (!request.workEnd().isAfter(request.workStart())) {
            throw new BusinessException("Horario de fim do expediente deve ser depois do horario de inicio");
        }
    }
}
