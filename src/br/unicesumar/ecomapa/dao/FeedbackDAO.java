package br.unicesumar.ecomapa.dao;

import br.unicesumar.ecomapa.model.Feedback;
import br.unicesumar.ecomapa.util.ConexaoMySQL;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso ao banco para a entidade Feedback (R08).
 *
 * Todo feedback é gravado com o cidadão que o enviou e o ponto avaliado
 * (RN07): sem essas duas chaves o banco recusa o registro.
 */
public class FeedbackDAO {

    /**
     * Grava um feedback enviado por um cidadão sobre um ponto de coleta.
     *
     * @param idUsuario     autor do feedback.
     * @param idPontoColeta ponto avaliado.
     */
    public boolean inserir(Feedback feedback, int idUsuario, int idPontoColeta) {
        String sql = "INSERT INTO feedback (descricao, data, status, idUsuario, idPontoColeta) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt =
                     conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, feedback.getDescricao());
            stmt.setDate(2, Date.valueOf(feedback.getData()));
            stmt.setString(3, feedback.getStatus());
            stmt.setInt(4, idUsuario);
            stmt.setInt(5, idPontoColeta);
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    feedback.setId(chaves.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao inserir o feedback: " + e.getMessage());
            return false;
        }
    }

    /** Lista os feedbacks de um ponto de coleta, do mais recente ao mais antigo. */
    public List<Feedback> listarPorPonto(int idPontoColeta) {
        String sql = "SELECT * FROM feedback WHERE idPontoColeta = ? ORDER BY data DESC, id DESC";
        return consultar(sql, idPontoColeta);
    }

    /** Lista os feedbacks enviados por um cidadão. */
    public List<Feedback> listarPorUsuario(int idUsuario) {
        String sql = "SELECT * FROM feedback WHERE idUsuario = ? ORDER BY data DESC, id DESC";
        return consultar(sql, idUsuario);
    }

    /**
     * Grava a mudança de situação do feedback, usada quando o administrador
     * o marca como analisado.
     */
    public boolean atualizarStatus(Feedback feedback) {
        String sql = "UPDATE feedback SET status = ? WHERE id = ?";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, feedback.getStatus());
            stmt.setInt(2, feedback.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar o feedback: " + e.getMessage());
            return false;
        }
    }

    /** Exclui um feedback. */
    public boolean excluir(int id) {
        String sql = "DELETE FROM feedback WHERE id = ?";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir o feedback: " + e.getMessage());
            return false;
        }
    }

    /** Executa uma consulta de feedbacks filtrada por um id. */
    private List<Feedback> consultar(String sql, int id) {
        List<Feedback> feedbacks = new ArrayList<>();

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    feedbacks.add(new Feedback(
                            rs.getInt("id"),
                            rs.getString("descricao"),
                            rs.getDate("data").toLocalDate(),
                            rs.getString("status")));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao consultar feedbacks: " + e.getMessage());
        }
        return feedbacks;
    }
}
