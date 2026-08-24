package com.agendamento.api.notification;

/**
 * Abstrai o envio de e-mails. Ter uma interface (em vez de chamar o SES direto
 * dos services de negocio) permite trocar o provedor de e-mail sem alterar
 * regra de negocio, e facilita testes (mock desta interface).
 */
public interface EmailService {
    void send(String to, String subject, String body);
}
