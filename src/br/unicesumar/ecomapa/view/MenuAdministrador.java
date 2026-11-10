package br.unicesumar.ecomapa.view;

import br.unicesumar.ecomapa.model.Feedback;
import br.unicesumar.ecomapa.model.Material;
import br.unicesumar.ecomapa.model.PontoColeta;
import br.unicesumar.ecomapa.model.Usuario;
import br.unicesumar.ecomapa.service.FeedbackService;
import br.unicesumar.ecomapa.service.MaterialService;
import br.unicesumar.ecomapa.service.PontoColetaService;

import java.util.List;
import java.util.Scanner;

/**
 * Área administrativa: CRUD de pontos de coleta e de materiais, e análise
 * dos feedbacks recebidos.
 *
 * Todas as operações passam pelos services, que conferem a permissão do
 * usuário (RN05) e validam os dados (RN01 e RN03) antes de gravar.
 */
public class MenuAdministrador {

    private final Scanner entrada;
    private final Usuario administrador;

    private final PontoColetaService pontoColetaService = new PontoColetaService();
    private final MaterialService materialService = new MaterialService();
    private final FeedbackService feedbackService = new FeedbackService();

    public MenuAdministrador(Scanner entrada, Usuario administrador) {
        this.entrada = entrada;
        this.administrador = administrador;
    }

    /** Exibe o menu administrativo até o usuário escolher sair. */
    public void exibir() {
        boolean executando = true;

        while (executando) {
            System.out.println();
            System.out.println("------------------------------------------");
            System.out.println("   AREA ADMINISTRATIVA - " + administrador.getNome());
            System.out.println("------------------------------------------");
            System.out.println("1 - Listar pontos de coleta");
            System.out.println("2 - Cadastrar ponto de coleta");
            System.out.println("3 - Atualizar ponto de coleta");
            System.out.println("4 - Excluir ponto de coleta");
            System.out.println("5 - Listar materiais");
            System.out.println("6 - Cadastrar material");
            System.out.println("7 - Analisar feedbacks de um ponto");
            System.out.println("0 - Sair da conta");
            System.out.print("Escolha uma opcao: ");

            String opcao = entrada.nextLine().trim();

            switch (opcao) {
                case "1" -> listarPontos();
                case "2" -> cadastrarPonto();
                case "3" -> atualizarPonto();
                case "4" -> excluirPonto();
                case "5" -> listarMateriais();
                case "6" -> cadastrarMaterial();
                case "7" -> analisarFeedbacks();
                case "0" -> executando = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    // ---------------------------------------------------------------
    // Pontos de coleta
    // ---------------------------------------------------------------

    /** Lista todos os pontos, inclusive os inativos. */
    private void listarPontos() {
        List<PontoColeta> pontos = pontoColetaService.listarTodos(administrador);

        if (pontos.isEmpty()) {
            System.out.println("Nenhum ponto de coleta cadastrado.");
            return;
        }

        System.out.println();
        for (PontoColeta ponto : pontos) {
            System.out.println("  " + ponto.getId() + " - " + ponto.getNome()
                    + (ponto.estaAtivo() ? "  [ativo]" : "  [inativo]"));
            System.out.println("      " + ponto.getEndereco()
                    + " | " + ponto.getHorarioFuncionamento()
                    + " | materiais: " + ponto.getMateriaisAceitos().size());
        }
    }

    /** Cadastra um ponto de coleta com seus materiais aceitos (R01, R03). */
    private void cadastrarPonto() {
        System.out.print("Nome: ");
        String nome = entrada.nextLine().trim();
        System.out.print("Endereco: ");
        String endereco = entrada.nextLine().trim();
        System.out.print("Telefone: ");
        String telefone = entrada.nextLine().trim();
        System.out.print("Horario de funcionamento: ");
        String horario = entrada.nextLine().trim();

        PontoColeta ponto = new PontoColeta(0, nome, endereco, telefone, horario, true);

        if (!vincularMateriais(ponto)) {
            return;
        }

        String erro = pontoColetaService.cadastrar(ponto, administrador);

        if (erro != null) {
            System.out.println(erro);
        } else {
            System.out.println("Ponto cadastrado com o codigo " + ponto.getId() + ".");
        }
    }

    /** Atualiza os dados e os materiais de um ponto já cadastrado (R06). */
    private void atualizarPonto() {
        PontoColeta ponto = escolherPonto();
        if (ponto == null) {
            return;
        }

        System.out.println("Deixe em branco para manter o valor atual.");

        System.out.print("Nome (" + ponto.getNome() + "): ");
        String nome = entrada.nextLine().trim();
        if (!nome.isEmpty()) {
            ponto.setNome(nome);
        }

        System.out.print("Endereco (" + ponto.getEndereco() + "): ");
        String endereco = entrada.nextLine().trim();
        if (!endereco.isEmpty()) {
            ponto.setEndereco(endereco);
        }

        System.out.print("Telefone (" + ponto.getTelefone() + "): ");
        String telefone = entrada.nextLine().trim();
        if (!telefone.isEmpty()) {
            ponto.setTelefone(telefone);
        }

        System.out.print("Horario (" + ponto.getHorarioFuncionamento() + "): ");
        String horario = entrada.nextLine().trim();
        if (!horario.isEmpty()) {
            ponto.setHorarioFuncionamento(horario);
        }

        System.out.print("Ponto ativo? (s/n, atual: "
                + (ponto.estaAtivo() ? "s" : "n") + "): ");
        String ativo = entrada.nextLine().trim();
        if (!ativo.isEmpty()) {
            ponto.setStatus(ativo.equalsIgnoreCase("s"));
        }

        System.out.print("Refazer os materiais aceitos? (s/n): ");
        if (entrada.nextLine().trim().equalsIgnoreCase("s")) {
            ponto.getMateriaisAceitos().clear();
            if (!vincularMateriais(ponto)) {
                return;
            }
        }

        String erro = pontoColetaService.atualizar(ponto, administrador);

        if (erro != null) {
            System.out.println(erro);
        } else {
            System.out.println("Ponto atualizado.");
        }
    }

    /** Exclui um ponto de coleta, com vínculos e feedbacks (R07). */
    private void excluirPonto() {
        PontoColeta ponto = escolherPonto();
        if (ponto == null) {
            return;
        }

        System.out.print("Confirma a exclusao de \"" + ponto.getNome()
                + "\" e de todos os seus feedbacks? (s/n): ");

        if (!entrada.nextLine().trim().equalsIgnoreCase("s")) {
            System.out.println("Exclusao cancelada.");
            return;
        }

        String erro = pontoColetaService.excluir(ponto.getId(), administrador);

        if (erro != null) {
            System.out.println(erro);
        } else {
            System.out.println("Ponto excluido.");
        }
    }

    // ---------------------------------------------------------------
    // Materiais
    // ---------------------------------------------------------------

    /** Lista os materiais cadastrados. */
    private void listarMateriais() {
        List<Material> materiais = materialService.listar();

        if (materiais.isEmpty()) {
            System.out.println("Nenhum material cadastrado.");
            return;
        }

        System.out.println();
        for (Material material : materiais) {
            System.out.println("  " + material.getId() + " - " + material.getNome()
                    + " | categoria: " + material.getCategoria());
        }
    }

    /** Cadastra um material, cujo nome deve ser único (R02, RN02). */
    private void cadastrarMaterial() {
        System.out.print("Nome do material: ");
        String nome = entrada.nextLine().trim();
        System.out.print("Descricao: ");
        String descricao = entrada.nextLine().trim();
        System.out.print("Categoria: ");
        String categoria = entrada.nextLine().trim();

        Material material = new Material(0, nome, descricao, categoria);
        String erro = materialService.cadastrar(material, administrador);

        if (erro != null) {
            System.out.println(erro);
        } else {
            System.out.println("Material cadastrado com o codigo " + material.getId() + ".");
        }
    }

    // ---------------------------------------------------------------
    // Feedbacks
    // ---------------------------------------------------------------

    /** Mostra os feedbacks de um ponto e permite marcá-los como analisados. */
    private void analisarFeedbacks() {
        PontoColeta ponto = escolherPonto();
        if (ponto == null) {
            return;
        }

        List<Feedback> feedbacks = feedbackService.listarPorPonto(ponto.getId());

        if (feedbacks.isEmpty()) {
            System.out.println("Este ponto ainda nao recebeu feedbacks.");
            return;
        }

        System.out.println();
        for (Feedback feedback : feedbacks) {
            System.out.println("  " + feedback.getId()
                    + " - [" + feedback.getData() + "] "
                    + feedback.getDescricao()
                    + "  (" + feedback.getStatus() + ")");
        }

        System.out.print("Codigo do feedback a marcar como analisado (0 para voltar): ");
        int id = lerNumero();
        if (id <= 0) {
            return;
        }

        for (Feedback feedback : feedbacks) {
            if (feedback.getId() == id) {
                String erro = feedbackService.marcarComoAnalisado(feedback, administrador);
                System.out.println(erro != null ? erro : "Feedback marcado como analisado.");
                return;
            }
        }
        System.out.println("Feedback nao encontrado na lista.");
    }

    // ---------------------------------------------------------------
    // Apoio
    // ---------------------------------------------------------------

    /** Mostra os pontos e devolve o escolhido pelo administrador. */
    private PontoColeta escolherPonto() {
        List<PontoColeta> pontos = pontoColetaService.listarTodos(administrador);

        if (pontos.isEmpty()) {
            System.out.println("Nenhum ponto de coleta cadastrado.");
            return null;
        }

        System.out.println();
        for (PontoColeta ponto : pontos) {
            System.out.println("  " + ponto.getId() + " - " + ponto.getNome());
        }
        System.out.print("Codigo do ponto: ");

        int id = lerNumero();
        for (PontoColeta ponto : pontos) {
            if (ponto.getId() == id) {
                return ponto;
            }
        }

        System.out.println("Ponto nao encontrado.");
        return null;
    }

    /**
     * Pede ao administrador os materiais aceitos pelo ponto.
     *
     * @return false se nao houver materiais cadastrados no sistema, caso em
     *         que o ponto nao pode ser gravado (RN03).
     */
    private boolean vincularMateriais(PontoColeta ponto) {
        List<Material> materiais = materialService.listar();

        if (materiais.isEmpty()) {
            System.out.println("Cadastre ao menos um material antes de cadastrar um ponto (RN03).");
            return false;
        }

        System.out.println();
        System.out.println("Materiais disponiveis:");
        for (Material material : materiais) {
            System.out.println("  " + material.getId() + " - " + material.getNome());
        }
        System.out.print("Codigos dos materiais aceitos, separados por virgula: ");

        String[] codigos = entrada.nextLine().split(",");

        for (String codigo : codigos) {
            try {
                int id = Integer.parseInt(codigo.trim());
                for (Material material : materiais) {
                    if (material.getId() == id) {
                        ponto.adicionarMaterial(material);
                    }
                }
            } catch (NumberFormatException e) {
                // codigo invalido e simplesmente ignorado
            }
        }

        System.out.println(ponto.getMateriaisAceitos().size() + " material(is) vinculado(s).");
        return true;
    }

    /** Le um numero do teclado, devolvendo -1 se o texto nao for numerico. */
    private int lerNumero() {
        try {
            return Integer.parseInt(entrada.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
