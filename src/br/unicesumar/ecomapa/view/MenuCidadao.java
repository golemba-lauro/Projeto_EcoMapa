package br.unicesumar.ecomapa.view;

import br.unicesumar.ecomapa.model.Cidadao;
import br.unicesumar.ecomapa.model.Feedback;
import br.unicesumar.ecomapa.model.Material;
import br.unicesumar.ecomapa.model.PontoColeta;
import br.unicesumar.ecomapa.service.FeedbackService;
import br.unicesumar.ecomapa.service.MaterialService;
import br.unicesumar.ecomapa.service.PontoColetaService;

import java.util.List;
import java.util.Scanner;

/**
 * Área do cidadão: consulta de pontos de coleta e envio de feedback.
 *
 * Nenhuma opção aqui depende de acesso administrativo (RN04), e os pontos
 * inativos nunca aparecem nas listagens (RN06).
 */
public class MenuCidadao {

    private final Scanner entrada;
    private final Cidadao cidadao;

    private final PontoColetaService pontoColetaService = new PontoColetaService();
    private final MaterialService materialService = new MaterialService();
    private final FeedbackService feedbackService = new FeedbackService();

    public MenuCidadao(Scanner entrada, Cidadao cidadao) {
        this.entrada = entrada;
        this.cidadao = cidadao;
    }

    /** Exibe o menu do cidadão até ele escolher voltar. */
    public void exibir() {
        boolean executando = true;

        while (executando) {
            System.out.println();
            System.out.println("------------------------------------------");
            System.out.println("   AREA DO CIDADAO - " + cidadao.getNome());
            System.out.println("------------------------------------------");
            System.out.println("1 - Ver todos os pontos de coleta");
            System.out.println("2 - Pesquisar pontos por material");
            System.out.println("3 - Ver materiais aceitos no sistema");
            System.out.println("4 - Enviar feedback sobre um ponto");
            System.out.println("5 - Ver meus feedbacks");
            System.out.println("0 - Sair da conta");
            System.out.print("Escolha uma opcao: ");

            String opcao = entrada.nextLine().trim();

            switch (opcao) {
                case "1" -> listarPontos();
                case "2" -> pesquisarPorMaterial();
                case "3" -> listarMateriais();
                case "4" -> enviarFeedback();
                case "5" -> listarMeusFeedbacks();
                case "0" -> executando = false;
                default -> System.out.println("Opcao invalida.");
            }
        }
    }

    /** Lista os pontos de coleta ativos, ordenados por nome (R04). */
    private void listarPontos() {
        List<PontoColeta> pontos = pontoColetaService.listarAtivos();

        if (pontos.isEmpty()) {
            System.out.println("Nenhum ponto de coleta disponivel no momento.");
            return;
        }

        System.out.println();
        System.out.println("Pontos de coleta disponiveis:");
        for (PontoColeta ponto : pontos) {
            mostrarPonto(ponto);
        }
    }

    /** Pesquisa os pontos ativos que aceitam determinado material (R05). */
    private void pesquisarPorMaterial() {
        System.out.print("Qual material voce quer descartar? ");
        String nome = entrada.nextLine().trim();

        List<PontoColeta> pontos = pontoColetaService.buscarPorMaterial(nome);

        if (pontos.isEmpty()) {
            System.out.println("Nenhum ponto ativo recebe \"" + nome + "\" no momento.");
            return;
        }

        System.out.println();
        System.out.println("Pontos que recebem \"" + nome + "\":");
        for (PontoColeta ponto : pontos) {
            mostrarPonto(ponto);
        }
    }

    /** Mostra os materiais cadastrados, para o cidadao saber o que pesquisar. */
    private void listarMateriais() {
        List<Material> materiais = materialService.listar();

        if (materiais.isEmpty()) {
            System.out.println("Nenhum material cadastrado ainda.");
            return;
        }

        System.out.println();
        System.out.println("Materiais aceitos no sistema:");
        for (Material material : materiais) {
            System.out.println("  - " + material.getNome()
                    + " (" + material.getCategoria() + ")");
        }
    }

    /** Registra um feedback do cidadao sobre um ponto de coleta (R08). */
    private void enviarFeedback() {
        List<PontoColeta> pontos = pontoColetaService.listarAtivos();

        if (pontos.isEmpty()) {
            System.out.println("Nao ha pontos disponiveis para avaliar.");
            return;
        }

        System.out.println();
        System.out.println("Escolha o ponto de coleta:");
        for (PontoColeta ponto : pontos) {
            System.out.println("  " + ponto.getId() + " - " + ponto.getNome());
        }
        System.out.print("Numero do ponto: ");

        int id = lerNumero();
        PontoColeta escolhido = procurarPorId(pontos, id);

        if (escolhido == null) {
            System.out.println("Ponto nao encontrado na lista.");
            return;
        }

        System.out.print("Escreva seu feedback: ");
        String descricao = entrada.nextLine();

        String erro = feedbackService.registrar(cidadao, escolhido, descricao);

        if (erro != null) {
            System.out.println(erro);
        } else {
            System.out.println("Feedback enviado. Obrigado pela contribuicao!");
        }
    }

    /** Lista os feedbacks ja enviados por este cidadao. */
    private void listarMeusFeedbacks() {
        List<Feedback> feedbacks = feedbackService.listarPorUsuario(cidadao.getId());

        if (feedbacks.isEmpty()) {
            System.out.println("Voce ainda nao enviou nenhum feedback.");
            return;
        }

        System.out.println();
        System.out.println("Seus feedbacks:");
        for (Feedback feedback : feedbacks) {
            System.out.println("  [" + feedback.getData() + "] "
                    + feedback.getDescricao()
                    + " (" + feedback.getStatus() + ")");
        }
    }

    /** Imprime os dados de um ponto de coleta e os materiais que ele aceita. */
    private void mostrarPonto(PontoColeta ponto) {
        System.out.println();
        System.out.println("  " + ponto.getId() + " - " + ponto.getNome());
        System.out.println("     Endereco: " + ponto.getEndereco());
        System.out.println("     Telefone: " + (ponto.getTelefone() == null
                ? "nao informado" : ponto.getTelefone()));
        System.out.println("     Horario:  " + ponto.getHorarioFuncionamento());

        StringBuilder materiais = new StringBuilder();
        for (Material material : ponto.getMateriaisAceitos()) {
            if (materiais.length() > 0) {
                materiais.append(", ");
            }
            materiais.append(material.getNome());
        }
        System.out.println("     Aceita:   " + (materiais.length() == 0
                ? "nenhum material vinculado" : materiais));
    }

    /** Procura um ponto pelo id dentro de uma lista ja carregada. */
    private PontoColeta procurarPorId(List<PontoColeta> pontos, int id) {
        for (PontoColeta ponto : pontos) {
            if (ponto.getId() == id) {
                return ponto;
            }
        }
        return null;
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
