package br.unicesumar.ecomapa.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centraliza a abertura e o fechamento da conexão com o banco ecomapa.
 *
 * Todas as classes DAO usam esta classe para obter uma conexão, de modo que
 * endereço, usuário e senha do banco ficam definidos em um único lugar.
 */
public class ConexaoMySQL {

    /** Endereço do banco: servidor, porta, nome do banco e parâmetros. */
    private static final String URL =
            "jdbc:mysql://localhost:3306/ecomapa"
            + "?useSSL=false"
            + "&characterEncoding=UTF-8"
            + "&serverTimezone=America/Sao_Paulo";

    private static final String USUARIO = "root";
    private static final String SENHA = "root";

    /** Classe utilitária: não deve ser instanciada. */
    private ConexaoMySQL() {
    }

    /**
     * Abre uma nova conexão com o banco.
     *
     * Quem chama é responsável por fechá-la, de preferência com
     * try-with-resources.
     *
     * @throws SQLException se o banco não responder ou os dados de acesso
     *                      estiverem incorretos.
     */
    public static Connection abrir() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    /**
     * Fecha a conexão informada, ignorando conexões nulas.
     *
     * Uma falha ao fechar não interrompe o programa: apenas é registrada,
     * porque a operação que usava a conexão já terminou.
     */
    public static void fechar(Connection conexao) {
        if (conexao != null) {
            try {
                conexao.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }
}
