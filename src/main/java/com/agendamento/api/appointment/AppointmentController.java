package com.agendamento.api.appointment;

import com.agendamento.api.appointment.dto.AppointmentRequest;
import com.agendamento.api.appointment.dto.AppointmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Agendamento, cancelamento e consulta de horarios")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @Operation(summary = "Lista todas as consultas")
    public ResponseEntity<List<AppointmentResponse>> findAll() {
        return ResponseEntity.ok(appointmentService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma consulta pelo id")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Lista as consultas de um paciente")
    public ResponseEntity<List<AppointmentResponse>> findByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.findByPatient(patientId));
    }

    @GetMapping("/professional/{professionalId}")
    @Operation(summary = "Lista as consultas de um profissional")
    public ResponseEntity<List<AppointmentResponse>> findByProfessional(@PathVariable Long professionalId) {
        return ResponseEntity.ok(appointmentService.findByProfessional(professionalId));
    }

    @PostMapping
    @Operation(summary = "Agenda uma nova consulta (valida expediente e conflito de horario, envia e-mail de confirmacao)")
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancela uma consulta (envia e-mail de cancelamento)")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancel(id));
    }
}
