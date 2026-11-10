package br.unicesumar.ecomapa;

import br.unicesumar.ecomapa.view.MenuPrincipal;

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
        try (Scanner entrada = new Scanner(System.in)) {
            new MenuPrincipal(entrada).exibir();
        }
    }
}
