package com.agendamento.api.exception;

/** Lancada quando uma regra de negocio e violada (ex: conflito de horario, fora do expediente). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
