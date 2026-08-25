package com.agendamento.api.auth;

import com.agendamento.api.auth.dto.AuthResponse;
import com.agendamento.api.auth.dto.LoginRequest;
import com.agendamento.api.auth.dto.RegisterRequest;
import com.agendamento.api.exception.BusinessException;
import com.agendamento.api.security.JwtService;
import com.agendamento.api.user.Role;
import com.agendamento.api.user.User;
import com.agendamento.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void deveRegistrarNovoUsuarioComSucesso() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@teste.com", "senha123");
        when(userRepository.existsByEmail("ana@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-da-senha");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("token-fake");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("token-fake");
        assertThat(response.role()).isEqualTo(Role.USER.name());
        verify(userRepository).save(argThat(u -> u.getPassword().equals("hash-da-senha")));
    }

    @Test
    void naoDeveRegistrarUsuarioComEmailJaCadastrado() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@teste.com", "senha123");
        when(userRepository.existsByEmail("ana@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void deveLogarComCredenciaisValidas() {
        LoginRequest request = new LoginRequest("ana@teste.com", "senha123");
        User user = User.builder().id(1L).name("Ana").email("ana@teste.com").password("hash").role(Role.USER).build();

        when(userRepository.findByEmail("ana@teste.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token-fake");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("token-fake");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void naoDeveLogarComCredenciaisInvalidas() {
        LoginRequest request = new LoginRequest("ana@teste.com", "senhaErrada");
        doThrow(new BadCredentialsException("Credenciais invalidas"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any(User.class));
    }
}
