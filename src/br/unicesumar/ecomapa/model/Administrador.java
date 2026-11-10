package br.unicesumar.ecomapa.model;

/*
 * Usuário responsável pela manutenção dos cadastros do EcoMapa.
 *
 * Herda de Usuario e reescreve exibirPermissoes() (polimorfismo). Somente
 * este tipo de usuário pode cadastrar, alterar ou excluir pontos de
 * coleta (RN05).
 */
public class Administrador extends Usuario {

    
    public static final String TIPO = "ADMINISTRADOR";

    public Administrador() {
        super(0, null, null, null, TIPO);
    }

    public Administrador(int id, String nome, String email, String senha) {
        super(id, nome, email, senha, TIPO);
    }

    @Override
    public String exibirPermissoes() {
        return "Administrador: cadastrar, alterar e excluir pontos de coleta e materiais, "
                + "vincular materiais aos pontos e gerenciar feedbacks.";
    }

    /**
     * Vincula um material aos materiais aceitos por um ponto (R03).
     * O vínculo repetido é recusado pelo próprio ponto de coleta.
     *
     * @return true se o material foi vinculado agora.
     */
    public boolean vincularMaterial(PontoColeta ponto, Material material) {
        if (ponto == null) {
            return false;
        }
        return ponto.adicionarMaterial(material);
    }
}
