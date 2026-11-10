package br.unicesumar.ecomapa.service;

import br.unicesumar.ecomapa.dao.PontoColetaDAO;
import br.unicesumar.ecomapa.model.PontoColeta;
import br.unicesumar.ecomapa.model.Usuario;

import java.util.List;

/**
 * Regras de negócio dos pontos de coleta.
 *
 * Toda operação de escrita passa por duas perguntas antes de chegar ao
 * banco: quem está pedindo tem permissão (RN05) e os dados estão completos
 * (RN01 e RN03).
 */
public class PontoColetaService {

    private final PontoColetaDAO pontoColetaDAO = new PontoColetaDAO();
    private final UsuarioService usuarioService = new UsuarioService();

    /**
     * Cadastra um ponto de coleta (R01).
     *
     * @param autor usuário logado, que precisa ser administrador (RN05).
     * @return mensagem de erro, ou null se o cadastro foi concluído.
     */
    public String cadastrar(PontoColeta ponto, Usuario autor) {
        String erro = validar(ponto, autor);
        if (erro != null) {
            return erro;
        }
        if (!pontoColetaDAO.inserir(ponto)) {
            return "Não foi possível cadastrar o ponto de coleta.";
        }
        return null;
    }

    /**
     * Atualiza um ponto de coleta já cadastrado (R06).
     *
     * @return mensagem de erro, ou null se a alteração foi concluída.
     */
    public String atualizar(PontoColeta ponto, Usuario autor) {
        String erro = validar(ponto, autor);
        if (erro != null) {
            return erro;
        }
        if (ponto.getId() <= 0) {
            return "Ponto de coleta sem identificador: não é possível atualizar.";
        }
        if (!pontoColetaDAO.atualizar(ponto)) {
            return "Não foi possível atualizar o ponto de coleta.";
        }
        return null;
    }

    /**
     * Exclui um ponto de coleta, com seus vínculos e feedbacks (R07).
     *
     * @return mensagem de erro, ou null se a exclusão foi concluída.
     */
    public String excluir(int id, Usuario autor) {
        if (!usuarioService.ehAdministrador(autor)) {
            return "Somente administradores podem excluir pontos de coleta.";
        }
        if (!pontoColetaDAO.excluir(id)) {
            return "Não foi possível excluir o ponto de coleta.";
        }
        return null;
    }

    /**
     * Lista os pontos ativos, para consulta do cidadão (R04).
     * Pontos inativos não aparecem (RN06).
     */
    public List<PontoColeta> listarAtivos() {
        return pontoColetaDAO.listarAtivos();
    }

    /**
     * Lista todos os pontos, inclusive os inativos, para o administrador
     * gerenciar.
     *
     * @return lista vazia se quem pediu não for administrador (RN05).
     */
    public List<PontoColeta> listarTodos(Usuario autor) {
        if (!usuarioService.ehAdministrador(autor)) {
            return List.of();
        }
        return pontoColetaDAO.listar();
    }

    /**
     * Pesquisa os pontos ativos que aceitam o material informado (R05).
     * Disponível ao cidadão sem acesso administrativo (RN04).
     */
    public List<PontoColeta> buscarPorMaterial(String nomeMaterial) {
        if (nomeMaterial == null || nomeMaterial.trim().isEmpty()) {
            return List.of();
        }
        return pontoColetaDAO.buscarPorMaterial(nomeMaterial.trim());
    }

    /**
     * Validações comuns ao cadastro e à atualização.
     *
     * @return a primeira regra violada, ou null se estiver tudo certo.
     */
    private String validar(PontoColeta ponto, Usuario autor) {
        if (!usuarioService.ehAdministrador(autor)) {
            return "Somente administradores podem cadastrar ou alterar pontos de coleta.";
        }
        if (ponto == null) {
            return "Ponto de coleta não informado.";
        }
        // RN01: nome, endereço e horário de funcionamento são obrigatórios
        if (vazio(ponto.getNome())) {
            return "O nome do ponto de coleta é obrigatório.";
        }
        if (vazio(ponto.getEndereco())) {
            return "O endereço é obrigatório.";
        }
        if (vazio(ponto.getHorarioFuncionamento())) {
            return "O horário de funcionamento é obrigatório.";
        }
        // RN03: o ponto precisa aceitar ao menos um material
        if (ponto.getMateriaisAceitos().isEmpty()) {
            return "Vincule ao menos um material aceito pelo ponto de coleta.";
        }
        return null;
    }

    /** Texto nulo, vazio ou só com espaços. */
    private boolean vazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
