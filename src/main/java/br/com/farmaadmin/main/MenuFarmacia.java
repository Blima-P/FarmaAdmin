package br.com.farmaadmin.main;

import java.util.Scanner;
import br.com.farmaadmin.dao.ProdutoDAO;
import br.com.farmaadmin.dao.PedidoDAO;
import br.com.farmaadmin.modelo.Usuario;
import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.modelo.Pedido;
import br.com.farmaadmin.modelo.ItemPedido;
import java.util.List;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class MenuFarmacia {

    private final Scanner scanner;
    private final ProdutoDAO produtoDAO;
    private final PedidoDAO pedidoDAO;
    private final Usuario farmaciaLogada;
    private final int farmaciaId;
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MenuFarmacia(Usuario farmaciaLogada) {
        this(farmaciaLogada, new Scanner(System.in));
    }

    // Overloaded constructor to allow scripted input
    public MenuFarmacia(Usuario farmaciaLogada, Scanner scanner) {
        this.scanner = scanner;
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

    // Implementação completa dos métodos de CRUD de Produto para a farmácia

    private void listarMeusProdutos() {
        System.out.println("\n--- MEUS PRODUTOS (Farmácia ID: " + this.farmaciaId + ") ---");
        try {
            List<Produto> produtos = produtoDAO.listarTodos();
            boolean any = false;
            System.out.printf("%-5s | %-30s | %-10s | %-5s%n", "ID", "Nome", "Preço", "Estoque");
            System.out.println("------------------------------------------------------------------");
            for (Produto p : produtos) {
                if (p.getFarmaciaId() == this.farmaciaId) {
                    any = true;
                    System.out.printf("%-5d | %-30s | R$%-8.2f | %-5d%n",
                            p.getId(), p.getNome(), p.getPreco(), p.getEstoque());
                }
            }
            if (!any) System.out.println("Você ainda não possui produtos cadastrados.");
        } catch (SQLException e) {
            System.err.println("Erro ao listar meus produtos: " + e.getMessage());
        }
    }

    private void adicionarNovoProduto() {
        System.out.println("\n--- ADICIONAR NOVO PRODUTO ---");
        try {
            System.out.print("Nome: ");
            String nome = scanner.nextLine();
            System.out.print("Descrição: ");
            String descricao = scanner.nextLine();
            System.out.print("Preço (ex: 9.90): ");
            double preco = Double.parseDouble(scanner.nextLine());
            System.out.print("Estoque inicial (inteiro): ");
            int estoque = Integer.parseInt(scanner.nextLine());
            System.out.print("Categoria: ");
            String categoria = scanner.nextLine();

            Produto p = new Produto(nome, descricao, preco, estoque, this.farmaciaId, categoria);
            Produto criado = produtoDAO.adicionar(p);
            if (criado != null) {
                System.out.println("✅ Produto cadastrado com sucesso. ID: " + criado.getId());
            } else {
                System.out.println("❌ Falha ao cadastrar produto.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Preço ou estoque incorreto.");
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar produto: " + e.getMessage());
        }
    }

    private Produto buscarProdutoPorIdUI() {
        System.out.print("Digite o ID do produto: ");
        try {
            String line = scanner.nextLine();
            int id = Integer.parseInt(line);
            Produto p = produtoDAO.buscarPorId(id);
            if (p == null) {
                System.out.println("Produto não encontrado.");
                return null;
            }
            if (p.getFarmaciaId() != this.farmaciaId) {
                System.out.println("Este produto não pertence à sua farmácia.");
                return null;
            }
            return p;
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Digite um número válido.");
            return null;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produto: " + e.getMessage());
            return null;
        }
    }

    private void atualizarProduto() {
        System.out.println("\n--- ATUALIZAR PRODUTO ---");
        Produto p = buscarProdutoPorIdUI();
        if (p == null) return;

        try {
            System.out.println("(Enter para manter o valor atual)");
            System.out.print("Nome atual [" + p.getNome() + "] -> Novo nome: ");
            String novoNome = scanner.nextLine();
            if (!novoNome.isBlank()) p.setNome(novoNome);

            System.out.print("Descrição atual [" + p.getDescricao() + "] -> Nova descrição: ");
            String novaDesc = scanner.nextLine();
            if (!novaDesc.isBlank()) p.setDescricao(novaDesc);

            System.out.print("Preço atual [" + p.getPreco() + "] -> Novo preço: ");
            String precoStr = scanner.nextLine();
            if (!precoStr.isBlank()) p.setPreco(Double.parseDouble(precoStr));

            System.out.print("Estoque atual [" + p.getEstoque() + "] -> Novo estoque: ");
            String estoqueStr = scanner.nextLine();
            if (!estoqueStr.isBlank()) p.setEstoque(Integer.parseInt(estoqueStr));

            System.out.print("Categoria atual [" + p.getCategoria() + "] -> Nova categoria: ");
            String novaCat = scanner.nextLine();
            if (!novaCat.isBlank()) p.setCategoria(novaCat);

            if (produtoDAO.atualizar(p)) {
                System.out.println("✅ Produto atualizado com sucesso.");
            } else {
                System.out.println("❌ Falha ao atualizar produto.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida para número.");
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
        }
    }

    private void deletarProduto() {
        System.out.println("\n--- DELETAR PRODUTO ---");
        Produto p = buscarProdutoPorIdUI();
        if (p == null) return;

        System.out.print("Confirmar exclusão de '" + p.getNome() + "' (S/N)? ");
        String conf = scanner.nextLine().toUpperCase();
        if (!conf.equals("S")) {
            System.out.println("Operação cancelada.");
            return;
        }

        try {
            if (produtoDAO.deletar(p.getId())) {
                System.out.println("✅ Produto deletado com sucesso.");
            } else {
                System.out.println("❌ Falha ao deletar produto.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao deletar produto: " + e.getMessage());
        }
    }

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
                        atualizarProduto();
                        break;
                    case 4:
                        deletarProduto();
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
