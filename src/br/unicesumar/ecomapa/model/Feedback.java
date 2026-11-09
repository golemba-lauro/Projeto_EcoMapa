package br.unicesumar.ecomapa.model;

import java.time.LocalDate;

/**
 * Feedback enviado por um cidadão sobre um ponto de coleta (R08).
 *
 * O feedback é parte do ponto de coleta: não existe fora dele e é removido
 * quando o ponto é excluído (composição, R07). O cidadão que o enviou é
 * apenas o autor.
 */
public class Feedback {

    /** Feedback recém-enviado, ainda não analisado pelo administrador. */
    public static final String PENDENTE = "PENDENTE";

    /** Feedback já analisado pelo administrador. */
    public static final String ANALISADO = "ANALISADO";

    private int id;
    private String descricao;
    private LocalDate data;
    private String status;

    public Feedback() {
    }

    public Feedback(int id, String descricao, LocalDate data, String status) {
        this.id = id;
        this.descricao = descricao;
        this.data = data;
        this.status = status;
    }

    /** Marca o feedback como analisado pelo administrador. */
    public void marcarComoAnalisado() {
        this.status = ANALISADO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
