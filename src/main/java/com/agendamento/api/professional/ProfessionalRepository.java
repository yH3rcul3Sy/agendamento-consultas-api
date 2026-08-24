package com.agendamento.api.professional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {
    boolean existsByEmail(String email);
}
