package br.unicesumar.ecomapa.service;

import br.unicesumar.ecomapa.dao.FeedbackDAO;
import br.unicesumar.ecomapa.model.Cidadao;
import br.unicesumar.ecomapa.model.Feedback;
import br.unicesumar.ecomapa.model.PontoColeta;
import br.unicesumar.ecomapa.model.Usuario;

import java.util.List;

/**
 * Regras de negócio dos feedbacks (R08).
 *
 * Garante a RN07: todo feedback nasce ligado a um cidadão e a um ponto de
 * coleta.
 */
public class FeedbackService {

    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final UsuarioService usuarioService = new UsuarioService();

    /**
     * Registra um feedback de um cidadão sobre um ponto de coleta (R08).
     *
     * O objeto Feedback é criado pelo próprio cidadão, com a data de hoje e
     * a situação pendente; aqui ele é validado e gravado.
     *
     * @return mensagem de erro, ou null se o registro foi concluído.
     */
    public String registrar(Cidadao autor, PontoColeta ponto, String descricao) {
        // RN07: o feedback precisa de um cidadão e de um ponto de coleta
        if (autor == null) {
            return "É preciso estar identificado para enviar um feedback.";
        }
        if (ponto == null || ponto.getId() <= 0) {
            return "Escolha um ponto de coleta para avaliar.";
        }
        if (descricao == null || descricao.trim().isEmpty()) {
            return "Escreva o feedback antes de enviar.";
        }

        Feedback feedback = autor.registrarFeedback(ponto, descricao.trim());
        if (!feedbackDAO.inserir(feedback, autor.getId(), ponto.getId())) {
            return "Não foi possível registrar o feedback.";
        }
        return null;
    }

    /**
     * Marca um feedback como analisado pelo administrador.
     *
     * @return mensagem de erro, ou null se a alteração foi concluída.
     */
    public String marcarComoAnalisado(Feedback feedback, Usuario autor) {
        if (!usuarioService.ehAdministrador(autor)) {
            return "Somente administradores podem analisar feedbacks.";
        }
        if (feedback == null || feedback.getId() <= 0) {
            return "Feedback não informado.";
        }
        feedback.marcarComoAnalisado();
        if (!feedbackDAO.atualizarStatus(feedback)) {
            return "Não foi possível atualizar o feedback.";
        }
        return null;
    }

    /** Lista os feedbacks recebidos por um ponto de coleta. */
    public List<Feedback> listarPorPonto(int idPontoColeta) {
        return feedbackDAO.listarPorPonto(idPontoColeta);
    }

    /** Lista os feedbacks enviados por um cidadão. */
    public List<Feedback> listarPorUsuario(int idUsuario) {
        return feedbackDAO.listarPorUsuario(idUsuario);
    }

    /**
     * Exclui um feedback.
     *
     * @return mensagem de erro, ou null se a exclusão foi concluída.
     */
    public String excluir(int id, Usuario autor) {
        if (!usuarioService.ehAdministrador(autor)) {
            return "Somente administradores podem excluir feedbacks.";
        }
        if (!feedbackDAO.excluir(id)) {
            return "Não foi possível excluir o feedback.";
        }
        return null;
    }
}
