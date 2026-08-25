package com.agendamento.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testa o fluxo completo: registro -> login -> cadastro de profissional
 * -> cadastro de paciente -> agendamento -> cancelamento, rodando contra
 * um banco H2 em memoria (nao usa o PostgreSQL real).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendamentoFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fluxoCompletoDeAgendamentoDeveFuncionarDePontaAPonta() throws Exception {
        String token = registrarERetornarToken("ana.integration@teste.com");
        String authHeader = "Bearer " + token;

        // Sem token, rota protegida deve retornar 403
        mockMvc.perform(get("/api/patients")).andExpect(status().isForbidden());

        // Cadastrar profissional
        String professionalBody = """
                {"name":"Dr. Carlos","specialty":"Clinico Geral","email":"carlos.it@clinica.com","workStart":"08:00:00","workEnd":"18:00:00"}
                """;
        MvcResult professionalResult = mockMvc.perform(post("/api/professionals")
                        .header("Authorization", authHeader)
                        .contentType("application/json")
                        .content(professionalBody))
                .andExpect(status().isCreated())
                .andReturn();
        long professionalId = objectMapper.readTree(professionalResult.getResponse().getContentAsString()).get("id").asLong();

        // Cadastrar paciente
        String patientBody = """
                {"name":"Joao","email":"joao.it@teste.com","phone":"11999999999"}
                """;
        MvcResult patientResult = mockMvc.perform(post("/api/patients")
                        .header("Authorization", authHeader)
                        .contentType("application/json")
                        .content(patientBody))
                .andExpect(status().isCreated())
                .andReturn();
        long patientId = objectMapper.readTree(patientResult.getResponse().getContentAsString()).get("id").asLong();

        // Agendar consulta numa segunda-feira as 10h, dentro do expediente
        String appointmentBody = """
                {"patientId": %d, "professionalId": %d, "dateTime": "2026-08-31T10:00:00", "durationMinutes": 30}
                """.formatted(patientId, professionalId);

        MvcResult appointmentResult = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", authHeader)
                        .contentType("application/json")
                        .content(appointmentBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SCHEDULED")))
                .andReturn();
        long appointmentId = objectMapper.readTree(appointmentResult.getResponse().getContentAsString()).get("id").asLong();

        // Mesmo horario, mesmo profissional -> conflito
        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", authHeader)
                        .contentType("application/json")
                        .content(appointmentBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ja existe uma consulta")));

        // Fora do expediente (20h) -> rejeitado
        String foraDoExpediente = """
                {"patientId": %d, "professionalId": %d, "dateTime": "2026-08-31T20:00:00", "durationMinutes": 30}
                """.formatted(patientId, professionalId);
        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", authHeader)
                        .contentType("application/json")
                        .content(foraDoExpediente))
                .andExpect(status().isUnprocessableEntity());

        // Fim de semana -> rejeitado (2026-08-29 e sabado)
        String fimDeSemana = """
                {"patientId": %d, "professionalId": %d, "dateTime": "2026-08-29T10:00:00", "durationMinutes": 30}
                """.formatted(patientId, professionalId);
        mockMvc.perform(post("/api/appointments")
                        .header("Authorization", authHeader)
                        .contentType("application/json")
                        .content(fimDeSemana))
                .andExpect(status().isUnprocessableEntity());

        // Cancelar a consulta original
        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/cancel")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELED")));

        // Cancelar de novo -> erro de negocio
        mockMvc.perform(patch("/api/appointments/" + appointmentId + "/cancel")
                        .header("Authorization", authHeader))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoDeveAcessarRotaAdminSemPermissaoDeAdmin() throws Exception {
        String token = registrarERetornarToken("usuariocomum@teste.com");
        String authHeader = "Bearer " + token;

        String patientBody = """
                {"name":"Paciente Teste","email":"paciente.admin.it@teste.com","phone":"11999999999"}
                """;
        MvcResult result = mockMvc.perform(post("/api/patients")
                        .header("Authorization", authHeader)
                        .contentType("application/json")
                        .content(patientBody))
                .andExpect(status().isCreated())
                .andReturn();
        long patientId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Usuario comum (role USER) tentando deletar -> rota exige ROLE_ADMIN
        mockMvc.perform(delete("/api/patients/" + patientId)
                        .header("Authorization", authHeader))
                .andExpect(status().isForbidden());
    }

    private String registrarERetornarToken(String email) throws Exception {
        String body = """
                {"name":"Usuario Teste","email":"%s","password":"senha123"}
                """.formatted(email);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }
}
