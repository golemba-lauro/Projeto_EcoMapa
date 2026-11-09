package br.unicesumar.ecomapa.dao;

import br.unicesumar.ecomapa.model.Administrador;
import br.unicesumar.ecomapa.model.Cidadao;
import br.unicesumar.ecomapa.model.Usuario;
import br.unicesumar.ecomapa.util.ConexaoMySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso ao banco para a entidade Usuario (R09).
 *
 * A tabela usuario guarda cidadãos e administradores juntos. A coluna tipo
 * indica qual é qual: ao ler uma linha, este DAO instancia Cidadao ou
 * Administrador conforme esse valor. É o ponto em que a herança do
 * diagrama de classes se reconecta aos dados.
 */
public class UsuarioDAO {

    /**
     * Grava um novo usuário.
     *
     * @return true se gravou; false se o e-mail já está cadastrado ou se
     *         houve falha no banco.
     */
    public boolean inserir(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, email, senha, tipo) VALUES (?, ?, ?, ?)";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt =
                     conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getTipo());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    usuario.setId(chaves.getInt(1));
                }
            }
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("Já existe um usuário com o e-mail \"" + usuario.getEmail() + "\".");
            return false;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir o usuário: " + e.getMessage());
            return false;
        }
    }

    /**
     * Busca um usuário pelo e-mail, que é único na tabela.
     *
     * É a consulta usada na autenticação (R09): a camada de serviço compara
     * a senha informada com a do usuário devolvido aqui.
     *
     * @return o usuário, ou null se o e-mail não estiver cadastrado.
     */
    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montarUsuario(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar o usuário: " + e.getMessage());
        }
        return null;
    }

    /** Lista todos os usuários cadastrados, ordenados por nome. */
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario ORDER BY nome";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(montarUsuario(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
        }
        return usuarios;
    }

    /** Atualiza nome, e-mail e senha de um usuário já cadastrado. */
    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nome = ?, email = ?, senha = ? WHERE id = ?";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setInt(4, usuario.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("Já existe um usuário com o e-mail \"" + usuario.getEmail() + "\".");
            return false;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar o usuário: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exclui um usuário.
     *
     * O banco recusa a exclusão se o usuário tiver feedbacks registrados
     * (ON DELETE RESTRICT na tabela feedback), preservando o histórico.
     */
    public boolean excluir(int id) {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("O usuário possui feedbacks registrados e não pode ser excluído.");
            return false;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir o usuário: " + e.getMessage());
            return false;
        }
    }

    /**
     * Transforma a linha atual do ResultSet no objeto certo: a coluna tipo
     * decide se o usuário é um Cidadao ou um Administrador.
     */
    private Usuario montarUsuario(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String email = rs.getString("email");
        String senha = rs.getString("senha");
        String tipo = rs.getString("tipo");

        if (Administrador.TIPO.equals(tipo)) {
            return new Administrador(id, nome, email, senha);
        }
        return new Cidadao(id, nome, email, senha);
    }
}
