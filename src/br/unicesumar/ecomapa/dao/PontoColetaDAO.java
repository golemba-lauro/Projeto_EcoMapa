package br.unicesumar.ecomapa.dao;

import br.unicesumar.ecomapa.model.Material;
import br.unicesumar.ecomapa.model.PontoColeta;
import br.unicesumar.ecomapa.util.ConexaoMySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso ao banco para a entidade PontoColeta.
 *
 * Concentra todo o SQL da entidade principal do sistema: as quatro
 * operações do CRUD e a pesquisa por material (R05). Os vínculos com os
 * materiais aceitos, guardados na tabela associativa ponto_material, são
 * gravados junto com o ponto.
 */
public class PontoColetaDAO {

    /**
     * Grava um novo ponto de coleta e seus materiais aceitos (R01, R03).
     *
     * O ponto e seus vínculos são gravados em uma única transação: se algum
     * vínculo falhar, o ponto também não é gravado.
     *
     * @return true se a gravação foi concluída.
     */
    public boolean inserir(PontoColeta ponto) {
        String sql = "INSERT INTO ponto_coleta "
                + "(nome, endereco, telefone, horarioFuncionamento, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        Connection conexao = null;
        try {
            conexao = ConexaoMySQL.abrir();
            conexao.setAutoCommit(false);

            try (PreparedStatement stmt =
                         conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, ponto.getNome());
                stmt.setString(2, ponto.getEndereco());
                stmt.setString(3, ponto.getTelefone());
                stmt.setString(4, ponto.getHorarioFuncionamento());
                stmt.setBoolean(5, ponto.isStatus());
                stmt.executeUpdate();

                try (ResultSet chaves = stmt.getGeneratedKeys()) {
                    if (chaves.next()) {
                        ponto.setId(chaves.getInt(1));
                    }
                }
            }

            gravarMateriais(conexao, ponto);
            conexao.commit();
            return true;

        } catch (SQLException e) {
            desfazer(conexao);
            System.err.println("Erro ao inserir o ponto de coleta: " + e.getMessage());
            return false;
        } finally {
            ConexaoMySQL.fechar(conexao);
        }
    }

    /**
     * Lista todos os pontos cadastrados, ordenados por nome.
     * Usada pelo administrador, que precisa ver inclusive os inativos.
     */
    public List<PontoColeta> listar() {
        return consultar("SELECT * FROM ponto_coleta ORDER BY nome", null);
    }

    /**
     * Lista apenas os pontos ativos, ordenados por nome (R04).
     * Pontos inativos não aparecem como opção de descarte (RN06).
     */
    public List<PontoColeta> listarAtivos() {
        return consultar("SELECT * FROM ponto_coleta WHERE status = TRUE ORDER BY nome", null);
    }

    /**
     * Pesquisa os pontos ativos que aceitam o material informado (R05).
     *
     * O DISTINCT evita repetir o mesmo ponto quando a junção retorna mais de
     * uma linha para ele.
     */
    public List<PontoColeta> buscarPorMaterial(String nomeMaterial) {
        String sql = "SELECT DISTINCT p.* FROM ponto_coleta p "
                + "INNER JOIN ponto_material pm ON pm.idPontoColeta = p.id "
                + "INNER JOIN material m ON m.id = pm.idMaterial "
                + "WHERE m.nome = ? AND p.status = TRUE "
                + "ORDER BY p.nome";
        return consultar(sql, nomeMaterial);
    }

    /**
     * Atualiza os dados de um ponto já cadastrado e seus materiais (R06).
     *
     * Os vínculos antigos são apagados e regravados, também em uma única
     * transação.
     *
     * @return true se algum registro foi alterado.
     */
    public boolean atualizar(PontoColeta ponto) {
        String sql = "UPDATE ponto_coleta SET "
                + "nome = ?, endereco = ?, telefone = ?, "
                + "horarioFuncionamento = ?, status = ? "
                + "WHERE id = ?";

        Connection conexao = null;
        try {
            conexao = ConexaoMySQL.abrir();
            conexao.setAutoCommit(false);

            int alteradas;
            try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
                stmt.setString(1, ponto.getNome());
                stmt.setString(2, ponto.getEndereco());
                stmt.setString(3, ponto.getTelefone());
                stmt.setString(4, ponto.getHorarioFuncionamento());
                stmt.setBoolean(5, ponto.isStatus());
                stmt.setInt(6, ponto.getId());
                alteradas = stmt.executeUpdate();
            }

            try (PreparedStatement limpar = conexao.prepareStatement(
                    "DELETE FROM ponto_material WHERE idPontoColeta = ?")) {
                limpar.setInt(1, ponto.getId());
                limpar.executeUpdate();
            }
            gravarMateriais(conexao, ponto);

            conexao.commit();
            return alteradas > 0;

        } catch (SQLException e) {
            desfazer(conexao);
            System.err.println("Erro ao atualizar o ponto de coleta: " + e.getMessage());
            return false;
        } finally {
            ConexaoMySQL.fechar(conexao);
        }
    }

    /**
     * Exclui um ponto de coleta (R07).
     *
     * Os vínculos com materiais e os feedbacks do ponto são removidos pelo
     * próprio banco, pelas chaves estrangeiras com ON DELETE CASCADE.
     *
     * @return true se algum registro foi excluído.
     */
    public boolean excluir(int id) {
        String sql = "DELETE FROM ponto_coleta WHERE id = ?";

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir o ponto de coleta: " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------
    // Métodos de apoio
    // -----------------------------------------------------------------

    /**
     * Executa uma consulta que devolve pontos de coleta e carrega os
     * materiais aceitos por cada um.
     *
     * @param parametro valor do único ? da consulta, ou null se não houver.
     */
    private List<PontoColeta> consultar(String sql, String parametro) {
        List<PontoColeta> pontos = new ArrayList<>();

        try (Connection conexao = ConexaoMySQL.abrir();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {

            if (parametro != null) {
                stmt.setString(1, parametro);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pontos.add(montarPonto(rs));
                }
            }

            for (PontoColeta ponto : pontos) {
                carregarMateriais(conexao, ponto);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao consultar pontos de coleta: " + e.getMessage());
        }
        return pontos;
    }

    /** Transforma a linha atual do ResultSet em um objeto PontoColeta. */
    private PontoColeta montarPonto(ResultSet rs) throws SQLException {
        return new PontoColeta(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("endereco"),
                rs.getString("telefone"),
                rs.getString("horarioFuncionamento"),
                rs.getBoolean("status"));
    }

    /** Carrega, da tabela associativa, os materiais aceitos pelo ponto. */
    private void carregarMateriais(Connection conexao, PontoColeta ponto) throws SQLException {
        String sql = "SELECT m.* FROM material m "
                + "INNER JOIN ponto_material pm ON pm.idMaterial = m.id "
                + "WHERE pm.idPontoColeta = ? "
                + "ORDER BY m.nome";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, ponto.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ponto.adicionarMaterial(new Material(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descricao"),
                            rs.getString("categoria")));
                }
            }
        }
    }

    /** Grava na tabela associativa os materiais aceitos pelo ponto. */
    private void gravarMateriais(Connection conexao, PontoColeta ponto) throws SQLException {
        String sql = "INSERT INTO ponto_material (idPontoColeta, idMaterial) VALUES (?, ?)";

        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            for (Material material : ponto.getMateriaisAceitos()) {
                stmt.setInt(1, ponto.getId());
                stmt.setInt(2, material.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /** Desfaz a transação quando alguma etapa falha. */
    private void desfazer(Connection conexao) {
        if (conexao != null) {
            try {
                conexao.rollback();
            } catch (SQLException e) {
                System.err.println("Erro ao desfazer a transação: " + e.getMessage());
            }
        }
    }
}
