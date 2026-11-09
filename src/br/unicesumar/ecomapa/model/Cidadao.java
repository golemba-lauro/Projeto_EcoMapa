package br.unicesumar.ecomapa.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Usuário comum do EcoMapa: consulta pontos de coleta e envia feedbacks.
 *
 * Herda de Usuario e reescreve exibirPermissoes() (polimorfismo).
 */
public class Cidadao extends Usuario {

    
    public static final String TIPO = "CIDADAO";

    public Cidadao() {
        super(0, null, null, null, TIPO);
    }

    public Cidadao(int id, String nome, String email, String senha) {
        super(id, nome, email, senha, TIPO);
    }

    @Override
    public String exibirPermissoes() {
        return "Cidadão: consultar pontos de coleta, pesquisar materiais e enviar feedback.";
    }

    /*
     * Cria um feedback sobre um ponto de coleta e o vincula a ele (R08).
     * O feedback nasce com a data de hoje e situação pendente.
     *
     * @return o feedback criado, ou null se o ponto não for informado.
     */
    public Feedback registrarFeedback(PontoColeta ponto, String descricao) {
        if (ponto == null) {
            return null;
        }
        Feedback feedback = new Feedback(0, descricao, LocalDate.now(), Feedback.PENDENTE);
        ponto.adicionarFeedback(feedback);
        return feedback;
    }

    /*
     * Pesquisa os pontos ativos que aceitam o material informado (R05).     
     */
    public List<PontoColeta> consultarPontosPorMaterial(String nomeMaterial) {
        return null;
    }
}
