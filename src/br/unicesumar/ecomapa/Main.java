package br.unicesumar.ecomapa;

import br.unicesumar.ecomapa.view.MenuPrincipal;
import java.io.Console;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Ponto de entrada do EcoMapa.
 *
 * Cria o leitor do teclado, que é compartilhado por todos os menus, e
 * entrega o controle ao menu principal.
 */
public class Main {

    public static void main(String[] args) {
        // um unico Scanner para todo o sistema: abrir varios sobre System.in
        // causa problemas de leitura
        try (Scanner entrada = new Scanner(System.in, charsetDoConsole())) {
            new MenuPrincipal(entrada).exibir();
        }
    }

    /**
     * Descobre a codificação que o console usa.
     *
     * O Java adota UTF-8 por padrão, mas o console do Windows em português
     * entrega os caracteres em outra codificação. Sem este ajuste, palavras
     * digitadas com acento chegam corrompidas ao banco.
     *
     * @return a codificação do console, ou a padrão do sistema quando o
     *         programa não está ligado a um console (dentro de uma IDE,
     *         por exemplo).
     */
    private static Charset charsetDoConsole() {
        Console console = System.console();
        return console != null ? console.charset() : Charset.defaultCharset();
    }
}
