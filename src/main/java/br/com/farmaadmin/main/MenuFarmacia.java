package br.com.farmaadmin.main;

import br.com.farmaadmin.dao.PedidoDAO;
import br.com.farmaadmin.dao.ProdutoDAO;
import br.com.farmaadmin.modelo.ItemPedido;
import br.com.farmaadmin.modelo.Pedido;
import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.modelo.Usuario;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class MenuFarmacia {

    private final Scanner scanner;
    private final ProdutoDAO produtoDAO;
    private final PedidoDAO pedidoDAO;
    private final Usuario farmaciaLogada;
    private final int farmaciaId;
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MenuFarmacia(Usuario farmaciaLogada) {
        this.scanner = new Scanner(System.in);
        this.produtoDAO = new ProdutoDAO();
        this.pedidoDAO = new PedidoDAO();
        this.farmaciaLogada = farmaciaLogada;
        this.farmaciaId = farmaciaLogada.getId();
    }

    // --- Métodos de CRUD de Produto (Omitidos para brevidade, use a última versão completa) ---
    // Os métodos 'adicionarNovoProduto', 'listarMeusProdutos', 'atualizarProduto', 'deletarProduto' permanecem inalterados

    // --- NOVO MÉTODO: Ver Pedidos Recebidos ---

    private void verPedidosRecebidos() {
        System.out.println("\n--- PEDIDOS RECEBIDOS NA FARMÁCIA ID: " + this.farmaciaId + " ---");
        try {
            List<Pedido> pedidos = pedidoDAO.listarPedidosParaFarmacia(this.farmaciaId);

            if (pedidos.isEmpty()) {
                System.out.println("Nenhum pedido recebido que contenha seus produtos.");
                return;
            }

            for (Pedido p : pedidos) {
                System.out.println("------------------------------------");
                System.out.printf("PEDIDO #%-5d | Status: %-15s | Data: %s%n",
                        p.getId(), p.getStatus(), p.getDataPedido().format(DATE_FORMAT));
                System.out.printf("Valor Total: R$%.2f | Endereço: %s%n", p.getValorTotal(), p.getEnderecoEntrega());

                // Detalhes do Pedido - Mostra apenas os itens desta farmácia no pedido
                List<ItemPedido> todosItens = pedidoDAO.listarItensDoPedido(p.getId());
                System.out.println("  > ITENS DA SUA FARMÁCIA:");
                for (ItemPedido item : todosItens) {
                    if (item.getFarmaciaId() == this.farmaciaId) {
                        System.out.printf("    %-3dx %-30s | R$%.2f%n",
                                item.getQuantidade(), item.getNomeProduto(), item.getSubtotal());
                    }
                }

                System.out.println("------------------------------------");
            }

            atualizarStatusPedido(pedidos);

        } catch (SQLException e) {
            System.err.println("Erro ao buscar pedidos recebidos: " + e.getMessage());
        }
    }

    private void atualizarStatusPedido(List<Pedido> pedidos) throws SQLException {
        System.out.print("\nPara atualizar um status, digite o ID do Pedido (ou 0 para sair): ");
        String inputId = scanner.nextLine();

        if (inputId.equals("0") || inputId.isBlank()) return;

        try {
            int pedidoId = Integer.parseInt(inputId);
            Pedido pedido = pedidoDAO.buscarPorId(pedidoId);

            if (pedido == null || !pedidos.stream().anyMatch(p -> p.getId() == pedidoId)) {
                System.out.println("❌ ID do Pedido inválido ou não contém produtos desta farmácia.");
                return;
            }

            System.out.println("\n--- ATUALIZAR STATUS DO PEDIDO #" + pedidoId + " (Atual: " + pedido.getStatus() + ") ---");
            System.out.println("1. EM_PROCESSAMENTO");
            System.out.println("2. ENVIADO");
            System.out.println("3. ENTREGUE");
            System.out.println("4. CANCELADO");
            System.out.print("Escolha o novo status (1-4): ");
            String statusOpcao = scanner.nextLine();

            String novoStatus = switch (statusOpcao) {
                case "1" -> "EM_PROCESSAMENTO";
                case "2" -> "ENVIADO";
                case "3" -> "ENTREGUE";
                case "4" -> "CANCELADO";
                default -> {
                    System.out.println("❌ Opção de status inválida.");
                    yield null;
                }
            };

            if (novoStatus != null) {
                if (pedidoDAO.atualizarStatus(pedidoId, novoStatus)) {
                    System.out.println("✅ Status do Pedido #" + pedidoId + " atualizado para: " + novoStatus);
                } else {
                    System.out.println("❌ Falha ao atualizar status.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida.");
        }
    }


    private void sair() {
        System.out.println("\nVoltando ao Menu Principal...");
    }

    // --- Os métodos de CRUD de Produto (adicionar, listar, atualizar, deletar) devem ser mantidos da última versão completa ---

    // Este é um stub que você deve substituir pelo código completo de CRUD de Produto
    // (Apenas para garantir que a opção 3 e 4 estão ligadas ao método correto)
    private void atualizarProduto() { /* ... lógica de CRUD ... */ }
    private void deletarProduto() { /* ... lógica de CRUD ... */ }
    private Produto buscarProdutoPorIdUI() { /* ... lógica de CRUD ... */ return null; }
    private void adicionarNovoProduto() { /* ... lógica de CRUD ... */ }
    private void listarMeusProdutos() { /* ... lógica de CRUD ... */ }

    // --- Menu Principal da Farmácia ---

    public void exibirMenu() {
        // ... (código de exibição do menu) ...
        int opcao = -1;

        System.out.println("\n------------------------------------");
        System.out.println("  GERENCIAMENTO DE PRODUTOS - FARMÁCIA " + farmaciaLogada.getNome());
        System.out.println("------------------------------------");

        while (opcao != 0) {
            System.out.println("\n--- OPÇÕES DE GERENCIAMENTO ---");
            System.out.println("1. Adicionar Novo Produto");
            System.out.println("2. Ver Meus Produtos");
            System.out.println("3. Atualizar Produto (Preço/Estoque/Nome)");
            System.out.println("4. Deletar Produto");
            System.out.println("5. Ver e Gerenciar Pedidos Recebidos"); // NOVO
            System.out.println("0. Voltar ao Menu Principal");
            System.out.print("Escolha uma opção: ");

            try {
                String input = scanner.nextLine();
                if (input.isEmpty()) continue;
                opcao = Integer.parseInt(input);

                switch (opcao) {
                    case 1:
                        adicionarNovoProduto(); // Use o método completo da versão anterior
                        break;
                    case 2:
                        listarMeusProdutos(); // Use o método completo da versão anterior
                        break;
                    case 3:
                        // Você deve inserir aqui o código completo do método 'atualizarProduto' da versão anterior
                        System.out.println("MÉTODO 3 (Atualizar) - Insira o código completo do Módulo 2 aqui.");
                        // atualizarProduto();
                        break;
                    case 4:
                        // Você deve inserir aqui o código completo do método 'deletarProduto' da versão anterior
                        System.out.println("MÉTODO 4 (Deletar) - Insira o código completo do Módulo 2 aqui.");
                        // deletarProduto();
                        break;
                    case 5:
                        verPedidosRecebidos();
                        break;
                    case 0:
                        sair();
                        break;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número.");
            }
        }
    }
}