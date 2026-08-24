package com.agendamento.api.professional;

import com.agendamento.api.professional.dto.ProfessionalRequest;
import com.agendamento.api.professional.dto.ProfessionalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professionals")
@RequiredArgsConstructor
@Tag(name = "Profissionais", description = "Cadastro de profissionais e seus horarios de expediente")
@SecurityRequirement(name = "bearerAuth")
public class ProfessionalController {

    private final ProfessionalService professionalService;

    @GetMapping
    @Operation(summary = "Lista todos os profissionais")
    public ResponseEntity<List<ProfessionalResponse>> findAll() {
        return ResponseEntity.ok(professionalService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um profissional pelo id")
    public ResponseEntity<ProfessionalResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo profissional")
    public ResponseEntity<ProfessionalResponse> create(@Valid @RequestBody ProfessionalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(professionalService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um profissional")
    public ResponseEntity<ProfessionalResponse> update(@PathVariable Long id, @Valid @RequestBody ProfessionalRequest request) {
        return ResponseEntity.ok(professionalService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um profissional")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        professionalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
