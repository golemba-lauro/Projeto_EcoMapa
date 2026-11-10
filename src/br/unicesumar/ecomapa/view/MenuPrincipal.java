package br.unicesumar.ecomapa.view;

import br.unicesumar.ecomapa.model.Administrador;
import br.unicesumar.ecomapa.model.Cidadao;
import br.unicesumar.ecomapa.model.Usuario;
import br.unicesumar.ecomapa.service.UsuarioService;

import java.util.Scanner;

/**
 * Menu inicial do EcoMapa: entrada no sistema e direcionamento.
 *
 * Depois da autenticação, o usuário é levado para a área do cidadão ou
 * para a área administrativa, conforme o tipo com que foi cadastrado
 * (RN04 e RN05).
 */
public class MenuPrincipal {

    private final Scanner entrada;
    private final UsuarioService usuarioService = new UsuarioService();

    public MenuPrincipal(Scanner entrada) {
        this.entrada = entrada;
    }

    /** Exibe o menu inicial até o usuário escolher sair. */
    public void exibir() {
        boolean executando = true;

        while (executando) {
            System.out.println();
            System.out.println("==========================================");
            System.out.println("   ECOMAPA - O destino certo para cada residuo");
            System.out.println("==========================================");
            System.out.println("1 - Entrar");
            System.out.println("2 - Criar uma conta");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opcao: ");

            String opcao = entrada.nextLine().trim();

            switch (opcao) {
                case "1" -> entrar();
                case "2" -> criarConta();
                case "0" -> {
                    System.out.println("Ate logo!");
                    executando = false;
                }
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    /** Autentica o usuário e abre a área correspondente ao seu tipo (R09). */
    private void entrar() {
        System.out.print("E-mail: ");
        String email = entrada.nextLine().trim();
        System.out.print("Senha: ");
        String senha = entrada.nextLine().trim();

        Usuario usuario = usuarioService.autenticar(email, senha);

        if (usuario == null) {
            System.out.println("E-mail ou senha incorretos.");
            return;
        }

        System.out.println();
        System.out.println("Bem-vindo(a), " + usuario.getNome() + "!");
        System.out.println(usuario.exibirPermissoes());

        // o objeto devolvido pelo service ja e um Cidadao ou um Administrador:
        // e o polimorfismo decidindo qual area abrir
        if (usuario instanceof Administrador) {
            new MenuAdministrador(entrada, usuario).exibir();
        } else {
            new MenuCidadao(entrada, (Cidadao) usuario).exibir();
        }
    }

    /** Cadastra um novo usuário no sistema (R09). */
    private void criarConta() {
        System.out.print("Nome: ");
        String nome = entrada.nextLine().trim();
        System.out.print("E-mail: ");
        String email = entrada.nextLine().trim();
        System.out.print("Senha: ");
        String senha = entrada.nextLine().trim();

        System.out.println("Tipo de conta:");
        System.out.println("1 - Cidadao");
        System.out.println("2 - Administrador");
        System.out.print("Escolha: ");
        String tipo = entrada.nextLine().trim();

        Usuario novo = "2".equals(tipo)
                ? new Administrador(0, nome, email, senha)
                : new Cidadao(0, nome, email, senha);

        String erro = usuarioService.cadastrar(novo);

        if (erro != null) {
            System.out.println(erro);
        } else {
            System.out.println("Conta criada com sucesso. Agora e so entrar.");
        }
    }
}
