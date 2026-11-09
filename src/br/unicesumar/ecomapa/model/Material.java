package br.unicesumar.ecomapa.model;

/**
 * Material que pode ser entregue em um ponto de coleta: eletrônicos, pilhas
 * e baterias, óleo de cozinha, roupas, plástico, vidro ou papelão.
 *
 * O nome é único dentro do sistema (RN02).
 */
public class Material {

    private int id;
    private String nome;
    private String descricao;
    private String categoria;

    public Material() {
    }

    public Material(int id, String nome, String descricao, String categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
