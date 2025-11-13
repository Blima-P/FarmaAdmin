package br.com.farmaadmin.main;

import br.com.farmaadmin.dao.PedidoDAO;
import br.com.farmaadmin.dao.ProdutoDAO;
import br.com.farmaadmin.modelo.ItemPedido;
import br.com.farmaadmin.modelo.Pedido;
import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.modelo.Usuario;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MenuCliente {

    private final Scanner scanner;
    private final ProdutoDAO produtoDAO;
    private final PedidoDAO pedidoDAO;
    private final Usuario clienteLogado;
    private final List<Produto> carrinho;
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MenuCliente(Usuario clienteLogado) {
        this.scanner = new Scanner(System.in);
        this.produtoDAO = new ProdutoDAO();
        this.pedidoDAO = new PedidoDAO();
        this.clienteLogado = clienteLogado;
        this.carrinho = new ArrayList<>();

        System.out.println("\n------------------------------------");
        System.out.println("  MARKETPLACE DROGARIA EXPRESS - Cliente " + clienteLogado.getNome());
        System.out.println("------------------------------------");
    }

    private void listarProdutosDisponiveis() {
        System.out.println("\n--- PRODUTOS EM TODAS AS FARMÁCIAS ---");
        try {
            List<Produto> produtos = produtoDAO.listarTodos();
            if (produtos.isEmpty()) {
                System.out.println("Nenhum produto cadastrado no momento.");
                return;
            }

            System.out.printf("%-5s | %-30s | %-10s | %-5s%n", "ID", "Nome", "Preço", "Estoque");
            System.out.println("------------------------------------------------------------------");
            for (Produto p : produtos) {
                // Não mostra produtos com estoque zero
                if (p.getEstoque() > 0) {
                    System.out.printf("%-5d | %-30s | R$%-8.2f | %-5d%n",
                            p.getId(), p.getNome(), p.getPreco(), p.getEstoque());
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar produtos: " + e.getMessage());
        }
    }

    private void adicionarAoCarrinho() {
        listarProdutosDisponiveis();
        System.out.print("\nDigite o ID do produto para adicionar: ");
        try {
            int produtoId = Integer.parseInt(scanner.nextLine());
            Produto produto = produtoDAO.buscarPorId(produtoId);

            if (produto == null || produto.getEstoque() <= 0) {
                System.out.println("❌ Produto não encontrado ou sem estoque.");
                return;
            }

            System.out.print("Quantidade desejada (Máx: " + produto.getEstoque() + "): ");
            int quantidade = Integer.parseInt(scanner.nextLine());

            if (quantidade > 0 && quantidade <= produto.getEstoque()) {
                // Clonagem simplificada: 'estoque' no Produto vira a 'quantidade' no carrinho.
                Produto itemCarrinho = new Produto(produto.getNome(), produto.getDescricao(), produto.getPreco(), quantidade, produto.getFarmaciaId(), produto.getCategoria());
                itemCarrinho.setId(produto.getId());
                carrinho.add(itemCarrinho);
                System.out.println("✅ " + quantidade + "x '" + produto.getNome() + "' adicionado ao carrinho.");
            } else {
                System.out.println("❌ Quantidade inválida. A quantidade deve ser maior que 0 e menor ou igual ao estoque.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Digite um número.");
        } catch (SQLException e) {
            System.err.println("Erro de BD: " + e.getMessage());
        }
    }

    private void verCarrinho() {
        if (carrinho.isEmpty()) {
            System.out.println("Seu carrinho está vazio.");
            return;
        }
        System.out.println("\n--- SEU CARRINHO ---");
        double total = 0;
        for (int i = 0; i < carrinho.size(); i++) {
            Produto item = carrinho.get(i);
            double subtotal = item.getPreco() * item.getEstoque();
            total += subtotal;
            System.out.printf("%d. %-30s | Qtd: %-3d | R$%-8.2f (Subtotal: R$%.2f)%n",
                    (i + 1), item.getNome(), item.getEstoque(), item.getPreco(), subtotal);
        }
        System.out.println("----------------------------------------------");
        System.out.printf("VALOR TOTAL DO PEDIDO: R$%.2f%n", total);
    }

    private void finalizarPedido() {
        if (carrinho.isEmpty()) {
            System.out.println("❌ O carrinho está vazio. Adicione produtos antes de finalizar.");
            return;
        }

        verCarrinho();

        System.out.print("\nDigite seu endereço de entrega: ");
        String endereco = scanner.nextLine();

        System.out.print("Confirmar pedido no valor total (S/N)? ");
        String confirmacao = scanner.nextLine().toUpperCase();

        if (confirmacao.equals("S")) {
            double total = carrinho.stream()
                    .mapToDouble(p -> p.getPreco() * p.getEstoque())
                    .sum();

            Pedido novoPedido = new Pedido();
            novoPedido.setUsuarioId(clienteLogado.getId());
            novoPedido.setValorTotal(total);
            novoPedido.setEnderecoEntrega(endereco);

            try {
                novoPedido = pedidoDAO.registrarNovoPedido(novoPedido, carrinho);

                System.out.println("\n🎉 PEDIDO FINALIZADO COM SUCESSO! 🎉");
                System.out.println("Seu número de pedido é: " + novoPedido.getId());
                System.out.println("Status inicial: " + novoPedido.getStatus());

                carrinho.clear();

            } catch (SQLException e) {
                System.err.println("❌ Erro ao finalizar o pedido. O estoque de algum item pode ter acabado. Detalhes: " + e.getMessage());
            }

        } else {
            System.out.println("Pedido cancelado.");
        }
    }

    private void verMeusPedidos() {
        System.out.println("\n--- HISTÓRICO DE PEDIDOS ---");
        try {
            List<Pedido> pedidos = pedidoDAO.listarPedidosPorUsuario(clienteLogado.getId());

            if (pedidos.isEmpty()) {
                System.out.println("Você ainda não fez nenhum pedido.");
                return;
            }

            for (Pedido p : pedidos) {
                System.out.println("------------------------------------");
                System.out.printf("PEDIDO #%-5d | Status: %-15s | Data: %s%n",
                        p.getId(), p.getStatus(), p.getDataPedido().format(DATE_FORMAT));
                System.out.printf("Valor Total: R$%.2f | Entrega em: %s%n", p.getValorTotal(), p.getEnderecoEntrega());

                // Detalhes do Pedido
                List<ItemPedido> itens = pedidoDAO.listarItensDoPedido(p.getId());
                System.out.println("  > ITENS:");
                for (ItemPedido item : itens) {
                    System.out.printf("    %-3dx %-30s | R$%.2f (Farmácia ID: %d)%n",
                            item.getQuantidade(), item.getNomeProduto(), item.getSubtotal(), item.getFarmaciaId());
                }
            }
            System.out.println("------------------------------------");

        } catch (SQLException e) {
            System.err.println("Erro ao buscar histórico de pedidos: " + e.getMessage());
        }
    }

    // --- Menu Principal do Cliente ---

    public void exibirMenu() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n--- OPÇÕES DE COMPRA ---");
            System.out.println("1. Ver Todos os Produtos");
            System.out.println("2. Adicionar Produto ao Carrinho");
            System.out.println("3. Ver Carrinho (" + carrinho.size() + " itens)");
            System.out.println("4. Finalizar Pedido");
            System.out.println("5. Ver Meus Pedidos");
            System.out.println("0. Voltar ao Menu Principal");
            System.out.print("Escolha uma opção: ");

            try {
                String input = scanner.nextLine();
                if (input.isEmpty()) continue;
                opcao = Integer.parseInt(input);

                switch (opcao) {
                    case 1:
                        listarProdutosDisponiveis();
                        break;
                    case 2:
                        adicionarAoCarrinho();
                        break;
                    case 3:
                        verCarrinho();
                        break;
                    case 4:
                        finalizarPedido();
                        break;
                    case 5:
                        verMeusPedidos();
                        break;
                    case 0:
                        System.out.println("Desconectando do Marketplace...");
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