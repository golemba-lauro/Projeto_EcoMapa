package br.unicesumar.ecomapa.service;

import br.unicesumar.ecomapa.dao.UsuarioDAO;
import br.unicesumar.ecomapa.model.Administrador;
import br.unicesumar.ecomapa.model.Usuario;

import java.util.List;

/**
 * Regras de negócio dos usuários: cadastro, autenticação e verificação de
 * permissão (R09, RN05).
 *
 * Nenhuma outra camada decide quem pode o quê: a pergunta "este usuário é
 * administrador?" é respondida aqui.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Cadastra um usuário, validando os dados obrigatórios (R09).
     *
     * @return mensagem de erro, ou null se o cadastro foi concluído.
     */
    public String cadastrar(Usuario usuario) {
        if (usuario == null) {
            return "Usuário não informado.";
        }
        if (vazio(usuario.getNome())) {
            return "O nome é obrigatório.";
        }
        if (vazio(usuario.getEmail())) {
            return "O e-mail é obrigatório.";
        }
        if (!usuario.getEmail().contains("@")) {
            return "Informe um e-mail válido.";
        }
        if (vazio(usuario.getSenha())) {
            return "A senha é obrigatória.";
        }
        if (usuarioDAO.buscarPorEmail(usuario.getEmail()) != null) {
            return "Já existe um usuário cadastrado com este e-mail.";
        }
        if (!usuarioDAO.inserir(usuario)) {
            return "Não foi possível cadastrar o usuário.";
        }
        return null;
    }

    /**
     * Autentica um usuário pelo e-mail e senha (R09).
     *
     * @return o usuário autenticado — um Cidadao ou um Administrador,
     *         conforme o tipo gravado — ou null se os dados não conferirem.
     */
    public Usuario autenticar(String email, String senha) {
        if (vazio(email) || vazio(senha)) {
            return null;
        }
        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        if (usuario != null && usuario.getSenha().equals(senha)) {
            return usuario;
        }
        return null;
    }

    /**
     * Informa se o usuário tem permissão administrativa (RN05).
     *
     * Usado antes de qualquer operação de cadastro, alteração ou exclusão.
     */
    public boolean ehAdministrador(Usuario usuario) {
        return usuario instanceof Administrador;
    }

    /** Lista todos os usuários cadastrados. */
    public List<Usuario> listar() {
        return usuarioDAO.listar();
    }

    /** Texto nulo, vazio ou só com espaços. */
    private boolean vazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
