package com.example.jonas_project.compartilhado.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

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
        Objects.requireNonNull(valor, "Informe " + campo + ".");
        if (valor.signum() < 0) {
            throw new IllegalArgumentException(campo + " deve ser maior ou igual a zero.");
        }
        return valor.setScale(escala, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal decimalPositivo(BigDecimal valor, int escala, String campo) {
        BigDecimal resultado = decimalNaoNegativo(valor, escala, campo);
        if (resultado.signum() == 0) {
            throw new IllegalArgumentException(campo + " deve ser maior que zero.");
        }
        return resultado;
    }
}
