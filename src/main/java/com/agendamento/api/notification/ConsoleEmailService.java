package com.agendamento.api.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementacao usada em desenvolvimento: em vez de enviar um e-mail de
 * verdade, apenas registra no log. Assim da para rodar e testar a aplicacao
 * localmente sem precisar de uma conta AWS configurada.
 *
 * Ativa por padrao (app.notification.provider=console). Para usar o SES de
 * verdade, defina app.notification.provider=ses (veja README).
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.notification", name = "provider", havingValue = "console", matchIfMissing = true)
public class ConsoleEmailService implements EmailService {

    @Override
    public void send(String to, String subject, String body) {
        log.info("[EMAIL SIMULADO] Para: {} | Assunto: {}\n{}", to, subject, body);
    }
}
