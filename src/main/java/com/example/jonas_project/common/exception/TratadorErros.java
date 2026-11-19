package com.example.jonas_project.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorErros {

    /** O domínio sinaliza dados inválidos com IllegalArgumentException (ver ValidacaoDominio). */
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail dadosInvalidos(IllegalArgumentException erro) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, erro.getMessage());
    }
}
