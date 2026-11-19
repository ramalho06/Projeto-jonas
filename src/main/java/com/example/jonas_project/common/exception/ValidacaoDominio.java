package com.example.jonas_project.common.exception;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ValidacaoDominio {
    private ValidacaoDominio() {
    }

    public static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Informe " + campo + ".");
        }
        return valor.strip();
    }

    public static BigDecimal decimalNaoNegativo(BigDecimal valor, int escala, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("Informe " + campo + ".");
        }
        if (valor.signum() < 0) {
            throw new IllegalArgumentException(campo + " deve ser maior ou igual a zero.");
        }
        try {
            return valor.setScale(escala, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException erro) {
            throw new IllegalArgumentException(campo + " admite até " + escala + " casas decimais.", erro);
        }
    }

    public static BigDecimal decimalPositivo(BigDecimal valor, int escala, String campo) {
        BigDecimal resultado = decimalNaoNegativo(valor, escala, campo);
        if (resultado.signum() == 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero.");
        }
        return resultado;
    }
}
