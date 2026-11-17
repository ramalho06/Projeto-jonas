package com.example.jonas_project.estoque.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;
import java.util.Objects;

import static com.example.jonas_project.compartilhado.dominio.ValidacaoDominio.decimalPositivo;

@Embeddable
public record MaterialNecessario(
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "material_id", nullable = false) Material material,
        @Column(nullable = false, precision = 15, scale = 3) BigDecimal quantidade
) {
    public MaterialNecessario {
        Objects.requireNonNull(material, "Informe o material.");
        quantidade = decimalPositivo(quantidade, 3, "A quantidade necessária");
    }
}
