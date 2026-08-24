package com.agendamento.api.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

/**
 * Envia e-mails de verdade via AWS SES (Simple Email Service).
 *
 * Ativado com app.notification.provider=ses. As credenciais da AWS NAO ficam
 * no codigo nem no application.yml: o SDK as busca automaticamente via
 * variaveis de ambiente (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY) ou de um
 * profile configurado com `aws configure` (cadeia padrao de credenciais).
 *
 * Requisitos na conta AWS:
 * - O endereco em app.notification.ses.from-email precisa estar verificado no SES.
 * - Enquanto a conta SES estiver em modo sandbox, o destinatario tambem precisa
 *   estar verificado (ou solicite acesso de producao no console AWS).
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.notification", name = "provider", havingValue = "ses")
public class SesEmailService implements EmailService {

    private final SesClient sesClient;
    private final String fromEmail;

    public SesEmailService(
            @Value("${app.aws.region:us-east-1}") String region,
            @Value("${app.notification.ses.from-email}") String fromEmail
    ) {
        this.sesClient = SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        this.fromEmail = fromEmail;
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .source(fromEmail)
                    .destination(Destination.builder().toAddresses(to).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder().text(Content.builder().data(body).build()).build())
                            .build())
                    .build();

            sesClient.sendEmail(request);
            log.info("E-mail enviado via SES para {}", to);
        } catch (SesException e) {
            // Nao deixamos falha de e-mail derrubar a operacao principal
            // (ex: o agendamento ja foi salvo no banco). Apenas registramos o erro.
            log.error("Falha ao enviar e-mail via SES para {}: {}", to, e.awsErrorDetails().errorMessage());
        }
    }
}
