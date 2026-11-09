package br.unicesumar.ecomapa.model;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Ponto de coleta onde os resíduos podem ser entregues.
 *
 * Mantém a lista dos materiais que aceita (associação N:N, representada no
 * banco pela tabela ponto_material) e a lista dos seus feedbacks
 * (composição: os feedbacks não existem fora do ponto).
 */
public class PontoColeta {

    private int id;
    private String nome;
    private String endereco;
    private String telefone;
    private String horarioFuncionamento;
    private boolean status;
    private List<Material> materiaisAceitos;
    private List<Feedback> feedbacks;

    public PontoColeta() {
        this.materiaisAceitos = new ArrayList<>();
        this.feedbacks = new ArrayList<>();
    }

    public PontoColeta(int id, String nome, String endereco, String telefone,
                       String horarioFuncionamento, boolean status) {
        this();
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.telefone = telefone;
        this.horarioFuncionamento = horarioFuncionamento;
        this.status = status;
    }

    /**
     * Vincula um material ao ponto, recusando o vínculo repetido (R03).
     *
     * @return true se o material foi vinculado agora.
     */
    public boolean adicionarMaterial(Material material) {
        if (material == null || aceitaMaterial(material.getNome())) {
            return false;
        }
        return materiaisAceitos.add(material);
    }

    /**
     * Remove um material aceito pelo ponto.
     *
     * @return true se o material estava vinculado e foi removido.
     */
    public boolean removerMaterial(Material material) {
        return materiaisAceitos.remove(material);
    }

    /**
     * Informa se este ponto recebe o material indicado (R05).
     *
     * A comparação ignora maiúsculas, minúsculas e acentos, do mesmo modo
     * que a collation utf8mb4_general_ci do banco: quem pesquisa "plastico"
     * encontra "Plástico".
     */
    public boolean aceitaMaterial(String nomeMaterial) {
        if (nomeMaterial == null) {
            return false;
        }
        for (Material material : materiaisAceitos) {
            if (semAcento(nomeMaterial).equalsIgnoreCase(semAcento(material.getNome()))) {
                return true;
            }
        }
        return false;
    }

    /** Remove os acentos de um texto para permitir a comparação. */
    private String semAcento(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /** Guarda mais um feedback recebido por este ponto. */
    public void adicionarFeedback(Feedback feedback) {
        if (feedback != null) {
            feedbacks.add(feedback);
        }
    }

    /**
     * Informa se o ponto está disponível para descarte (RN06).
     * Pontos inativos não devem aparecer nas consultas do cidadão.
     */
    public boolean estaAtivo() {
        return status;
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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getHorarioFuncionamento() {
        return horarioFuncionamento;
    }

    public void setHorarioFuncionamento(String horarioFuncionamento) {
        this.horarioFuncionamento = horarioFuncionamento;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public List<Material> getMateriaisAceitos() {
        return materiaisAceitos;
    }

    public void setMateriaisAceitos(List<Material> materiaisAceitos) {
        this.materiaisAceitos = materiaisAceitos;
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }
}
