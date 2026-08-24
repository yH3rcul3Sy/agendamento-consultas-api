package com.agendamento.api.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/** Monta o conteudo dos e-mails de agendamento/cancelamento e delega o envio ao EmailService ativo. */
@Service
@RequiredArgsConstructor
public class AppointmentNotificationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");

    private final EmailService emailService;

    @Async
    public void notifyConfirmation(AppointmentEmailData data) {
        String subject = "Consulta agendada com sucesso";
        String body = """
                Ola, %s!

                Sua consulta foi agendada com sucesso.

                Profissional: %s (%s)
                Data e horario: %s
                Duracao: %d minutos

                Caso precise cancelar, entre em contato ou utilize o sistema.
                """.formatted(
                data.patientName(),
                data.professionalName(),
                data.professionalSpecialty(),
                data.dateTime().format(FORMATTER),
                data.durationMinutes()
        );

        emailService.send(data.patientEmail(), subject, body);
    }

    @Async
    public void notifyCancellation(AppointmentEmailData data) {
        String subject = "Consulta cancelada";
        String body = """
                Ola, %s!

                Sua consulta com %s, marcada para %s, foi cancelada.

                Caso queira reagendar, acesse o sistema novamente.
                """.formatted(
                data.patientName(),
                data.professionalName(),
                data.dateTime().format(FORMATTER)
        );

        emailService.send(data.patientEmail(), subject, body);
    }
}
