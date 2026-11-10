package br.unicesumar.ecomapa.service;

import br.unicesumar.ecomapa.dao.MaterialDAO;
import br.unicesumar.ecomapa.model.Material;
import br.unicesumar.ecomapa.model.Usuario;

import java.util.List;

/**
 * Regras de negócio dos materiais (R02).
 *
 * Garante o nome único exigido pela RN02 e restringe o cadastro aos
 * administradores (RN05).
 */
public class MaterialService {

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final UsuarioService usuarioService = new UsuarioService();

    /**
     * Cadastra um material (R02).
     *
     * @return mensagem de erro, ou null se o cadastro foi concluído.
     */
    public String cadastrar(Material material, Usuario autor) {
        String erro = validar(material, autor);
        if (erro != null) {
            return erro;
        }
        if (materialDAO.buscarPorNome(material.getNome()) != null) {
            return "Já existe um material cadastrado com este nome.";
        }
        if (!materialDAO.inserir(material)) {
            return "Não foi possível cadastrar o material.";
        }
        return null;
    }

    /**
     * Atualiza um material já cadastrado.
     *
     * @return mensagem de erro, ou null se a alteração foi concluída.
     */
    public String atualizar(Material material, Usuario autor) {
        String erro = validar(material, autor);
        if (erro != null) {
            return erro;
        }
        if (material.getId() <= 0) {
            return "Material sem identificador: não é possível atualizar.";
        }
        Material existente = materialDAO.buscarPorNome(material.getNome());
        if (existente != null && existente.getId() != material.getId()) {
            return "Já existe outro material cadastrado com este nome.";
        }
        if (!materialDAO.atualizar(material)) {
            return "Não foi possível atualizar o material.";
        }
        return null;
    }

    /**
     * Exclui um material. Os vínculos com pontos de coleta são removidos
     * pelo banco.
     *
     * @return mensagem de erro, ou null se a exclusão foi concluída.
     */
    public String excluir(int id, Usuario autor) {
        if (!usuarioService.ehAdministrador(autor)) {
            return "Somente administradores podem excluir materiais.";
        }
        if (!materialDAO.excluir(id)) {
            return "Não foi possível excluir o material.";
        }
        return null;
    }

    /** Lista todos os materiais cadastrados, para consulta de qualquer usuário. */
    public List<Material> listar() {
        return materialDAO.listar();
    }

    /** Busca um material pelo nome, ignorando maiúsculas e acentos. */
    public Material buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return null;
        }
        return materialDAO.buscarPorNome(nome.trim());
    }

    /** Validações comuns ao cadastro e à atualização. */
    private String validar(Material material, Usuario autor) {
        if (!usuarioService.ehAdministrador(autor)) {
            return "Somente administradores podem cadastrar ou alterar materiais.";
        }
        if (material == null) {
            return "Material não informado.";
        }
        if (material.getNome() == null || material.getNome().trim().isEmpty()) {
            return "O nome do material é obrigatório.";
        }
        return null;
    }
}
