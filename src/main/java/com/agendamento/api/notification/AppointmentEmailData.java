package com.agendamento.api.notification;

import java.time.LocalDateTime;

/**
 * Dados simples (nao gerenciados pelo JPA) usados para montar o e-mail.
 *
 * Importante: nao passamos a entidade Appointment diretamente para o metodo
 * @Async, porque ele roda em outra thread. Se a entidade ainda tiver relacoes
 * "lazy" (patient/professional) nao carregadas, tentar acessa-las fora da
 * thread/sessao original do Hibernate causa erro. Por isso extraimos os
 * dados aqui, ainda dentro da transacao original, e so entao disparamos o
 * envio assincrono.
 */
public record AppointmentEmailData(
        String patientName,
        String patientEmail,
        String professionalName,
        String professionalSpecialty,
        LocalDateTime dateTime,
        int durationMinutes
) {
}
