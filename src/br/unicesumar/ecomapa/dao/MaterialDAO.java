package br.unicesumar.ecomapa.dao;

import br.unicesumar.ecomapa.model.Material;
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
 * Acesso ao banco para a entidade Material (R02).
 *
 * O nome do material é único no sistema (RN02): a restrição está na tabela
 * e a violação é tratada no método inserir.
 */
public class MaterialDAO {

    /**
     * Grava um novo material.
     *
     * @return true se gravou; false se já existe material com o mesmo nome
     *         (RN02) ou se houve falha no banco.
     */
    public boolean inserir(Material material) {
        String sql = "INSERT INTO material (nome, descricao, categoria) VALUES (?, ?, ?)";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt =
                     conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, material.getNome());
            stmt.setString(2, material.getDescricao());
            stmt.setString(3, material.getCategoria());
            stmt.executeUpdate();

            try (ResultSet chaves = stmt.getGeneratedKeys()) {
                if (chaves.next()) {
                    material.setId(chaves.getInt(1));
                }
            }
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("Já existe um material com o nome \"" + material.getNome() + "\".");
            return false;
        } catch (SQLException e) {
            System.err.println("Erro ao inserir o material: " + e.getMessage());
            return false;
        }
    }

    /** Lista todos os materiais cadastrados, ordenados por nome. */
    public List<Material> listar() {
        List<Material> materiais = new ArrayList<>();
        String sql = "SELECT * FROM material ORDER BY nome";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                materiais.add(montarMaterial(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar materiais: " + e.getMessage());
        }
        return materiais;
    }

    /**
     * Busca um material pelo nome.
     *
     * A comparação é feita pelo banco, cuja collation ignora maiúsculas e
     * acentos: "plastico" encontra "Plástico".
     *
     * @return o material, ou null se não houver nenhum com esse nome.
     */
    public Material buscarPorNome(String nome) {
        String sql = "SELECT * FROM material WHERE nome = ?";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return montarMaterial(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar o material: " + e.getMessage());
        }
        return null;
    }

    /** Atualiza os dados de um material já cadastrado. */
    public boolean atualizar(Material material) {
        String sql = "UPDATE material SET nome = ?, descricao = ?, categoria = ? WHERE id = ?";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, material.getNome());
            stmt.setString(2, material.getDescricao());
            stmt.setString(3, material.getCategoria());
            stmt.setInt(4, material.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.err.println("Já existe um material com o nome \"" + material.getNome() + "\".");
            return false;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar o material: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exclui um material. Os vínculos com pontos de coleta são removidos
     * pelo banco (ON DELETE CASCADE na tabela ponto_material).
     */
    public boolean excluir(int id) {
        String sql = "DELETE FROM material WHERE id = ?";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir o material: " + e.getMessage());
            return false;
        }
    }

    /** Transforma a linha atual do ResultSet em um objeto Material. */
    private Material montarMaterial(ResultSet rs) throws SQLException {
        return new Material(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getString("categoria"));
    }
}
