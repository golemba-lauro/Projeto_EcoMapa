package br.unicesumar.ecomapa.model;

/**
 * Classe abstrata que representa um usuário do EcoMapa.
 *
 * Concentra os atributos comuns a Cidadao e Administrador (abstração e
 * herança). O atributo tipo funciona como coluna discriminadora: guarda
 * CIDADAO ou ADMINISTRADOR e mapeia esta hierarquia para a tabela usuario.
 *
 * Todos os atributos são privados e acessados por getters e setters
 * (encapsulamento).
 */
public abstract class Usuario {

    private int id;
    private String nome;
    private String email;
    private String senha;
    private String tipo;

    public Usuario() {
    }

    public Usuario(int id, String nome, String email, String senha, String tipo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
    }

    /**
     * Descreve as permissões do usuário no sistema.
     *
     * Método abstrato: cada subclasse é obrigada a fornecer sua própria
     * versão. A camada de visão trabalha com uma referência do tipo Usuario
     * e o comportamento executado depende do objeto instanciado
     * (polimorfismo).
     */
    public abstract String exibirPermissoes();

    /**
     * Confere se o e-mail e a senha informados são deste usuário (R09).
     *
     * TODO etapa 13: a validação real, com a senha vinda do banco, será feita
     * em UsuarioService.
     */
    public boolean autenticar(String email, String senha) {
        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
