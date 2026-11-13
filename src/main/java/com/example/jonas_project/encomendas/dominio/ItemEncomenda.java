package com.example.jonas_project.encomendas.dominio;

import com.example.jonas_project.estoque.dominio.MaterialNecessario;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import static com.example.jonas_project.compartilhado.dominio.ValidacaoDominio.texto;

@Entity
@Table(name = "itens_encomenda")
public class ItemEncomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "versao_id", nullable = false)
    private VersaoEncomenda versao;

    @Column(nullable = false)
    private int ordem;

    @Column(name = "tipo_produto", nullable = false)
    private String tipoProduto;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false, columnDefinition = "text")
    private String personalizacao;

    @ElementCollection
    @CollectionTable(name = "materiais_item_encomenda", joinColumns = @JoinColumn(name = "item_id", nullable = false))
    private List<MaterialNecessario> materiais = new ArrayList<>();

    protected ItemEncomenda() {
    }

    public ItemEncomenda(String tipoProduto, int quantidade, String personalizacao,
                         List<MaterialNecessario> materiais) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade do item deve ser positiva.");
        }
        this.tipoProduto = texto(tipoProduto, "o tipo do produto");
        this.quantidade = quantidade;
        this.personalizacao = texto(personalizacao, "a personalização");
        this.materiais.addAll(copiarMateriais(materiais));
    }

    ItemEncomenda copiarPara(VersaoEncomenda versao, int ordem) {
        ItemEncomenda copia = new ItemEncomenda(getTipoProduto(), getQuantidade(), getPersonalizacao(), getMateriais());
        copia.versao = versao;
        copia.ordem = ordem;
        return copia;
    }

    void alterar(ItemEncomenda novosDados) {
        String novoTipo = novosDados.getTipoProduto();
        int novaQuantidade = novosDados.getQuantidade();
        String novaPersonalizacao = novosDados.getPersonalizacao();
        List<MaterialNecessario> novosMateriais = copiarMateriais(novosDados.getMateriais());
        tipoProduto = novoTipo;
        quantidade = novaQuantidade;
        personalizacao = novaPersonalizacao;
        materiais.clear();
        materiais.addAll(novosMateriais);
    }

    private static List<MaterialNecessario> copiarMateriais(List<MaterialNecessario> materiais) {
        return materiais.stream()
                .map(material -> new MaterialNecessario(material.material(), material.quantidade()))
                .toList();
    }

    public Long getId() {
        return id;
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getPersonalizacao() {
        return personalizacao;
    }

    public List<MaterialNecessario> getMateriais() {
        return List.copyOf(materiais);
    }
}
