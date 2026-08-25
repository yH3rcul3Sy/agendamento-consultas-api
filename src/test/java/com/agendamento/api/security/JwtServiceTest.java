package com.agendamento.api.security;

import com.agendamento.api.user.Role;
import com.agendamento.api.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "dGVzdC1zZWNyZXQtdXNlZC1vbmx5LWluLWF1dG9tYXRlZC10ZXN0cy0xMjM0NQ==");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);

        user = User.builder().id(1L).name("Ana").email("ana@teste.com").password("hash").role(Role.USER).build();
    }

    @Test
    void deveGerarTokenComOEmailDoUsuarioComoSubject() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("ana@teste.com");
    }

    @Test
    void tokenGeradoParaOUsuarioDeveSerValidoParaEleMesmo() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void tokenNaoDeveSerValidoParaOutroUsuario() {
        String token = jwtService.generateToken(user);

        User outroUsuario = User.builder().id(2L).name("Bruno").email("bruno@teste.com").password("hash").role(Role.USER).build();

        assertThat(jwtService.isTokenValid(token, outroUsuario)).isFalse();
    }

    @Test
    void tokenComExpiracaoNoPassadoDeveSerInvalido() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }
}
