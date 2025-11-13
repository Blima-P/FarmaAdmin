package br.com.farmaadmin.main;

import br.com.farmaadmin.dao.ProdutoDAO;
import br.com.farmaadmin.dao.UsuarioDAO;
import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.modelo.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class MenuPrincipal {

    private final Scanner scanner;
    private final UsuarioDAO usuarioDAO;
    private final ProdutoDAO produtoDAO;
    private Usuario usuarioLogado;

    public MenuPrincipal() {
        this.scanner = new Scanner(System.in);
        this.usuarioDAO = new UsuarioDAO();
        this.produtoDAO = new ProdutoDAO();
        this.usuarioLogado = null;
    }

    private void realizarLogin() {
        System.out.println("\n----- LOGIN -----");
        System.out.print("E-mail: ");
        String email = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        try {
            Usuario usuario = usuarioDAO.buscarPorEmailESenha(email, senha);
            if (usuario != null) {
                this.usuarioLogado = usuario;
                System.out.println("✅ Login bem-sucedido! Bem-vindo(a), " + usuario.getNome() + ".");

                // LÓGICA DE REDIRECIONAMENTO COMPLETA:
                if ("FARMACIA".equals(usuario.getTipoUsuario())) {
                    MenuFarmacia menuFarmacia = new MenuFarmacia(usuario);
                    menuFarmacia.exibirMenu();
                } else if ("CLIENTE".equals(usuario.getTipoUsuario())) {
                    // Chamada do novo Menu Cliente
                    MenuCliente menuCliente = new MenuCliente(usuario);
                    menuCliente.exibirMenu();
                }

            } else {
                System.out.println("❌ Falha no Login. E-mail ou senha incorretos.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao tentar fazer login: " + e.getMessage());
        }
    }

    private void realizarCadastro() {
        System.out.println("\n----- CADASTRO NOVO USUÁRIO -----");
        System.out.print("Nome Completo / Nome da Farmácia: ");
        String nome = scanner.nextLine();
        System.out.print("E-mail: ");
        String email = scanner.nextLine();
        System.out.print("Senha (mínimo 6 caracteres): ");
        String senha = scanner.nextLine();

        String tipo = "FARMACIA";

        Usuario novo = new Usuario(nome, email, senha, tipo);

        try {
            novo = usuarioDAO.adicionar(novo);
            if (novo != null) {
                System.out.println("✅ Cadastro realizado com sucesso! ID: " + novo.getId());
            } else {
                System.out.println("❌ Falha no cadastro. Verifique os dados.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao realizar cadastro (E-mail já pode estar em uso): " + e.getMessage());
        }
    }

    private void listarProdutos() {
        System.out.println("\n----- PRODUTOS DISPONÍVEIS (Marketplace) -----");
        try {
            List<Produto> produtos = produtoDAO.listarTodos();
            if (produtos.isEmpty()) {
                System.out.println("Nenhum produto cadastrado no momento.");
                return;
            }

            System.out.printf("%-5s | %-30s | %-10s | %-5s%n", "ID", "Nome", "Preço", "Estoque");
            System.out.println("------------------------------------------------------------------");
            for (Produto p : produtos) {
                System.out.printf("%-5d | %-30s | R$%-8.2f | %-5d%n",
                        p.getId(), p.getNome(), p.getPreco(), p.getEstoque());
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar produtos: " + e.getMessage());
        }
    }

    public void exibirMenu() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n----- MENU PRINCIPAL FARMAADMIN -----");

            if (usuarioLogado == null) {
                System.out.println("1. Entrar (Fazer Login)");
                System.out.println("2. Novo Cadastro (Criar Conta Farmácia)");
            } else {
                System.out.println("Você está logado como: " + usuarioLogado.getNome() + " | Acesso: " + usuarioLogado.getTipoUsuario());
                System.out.println("1. (RE-LOGAR) Entrar nos Menus Internos");
                System.out.println("9. Logout / Deslogar");
            }

            System.out.println("3. Listar Produtos Disponíveis");
            System.out.println("0. Sair da Aplicação");
            System.out.print("Escolha uma opção: ");

            try {
                String input = scanner.nextLine();
                if (input.isEmpty()) continue;
                opcao = Integer.parseInt(input);

                switch (opcao) {
                    case 1:
                        realizarLogin();
                        break;
                    case 2:
                        if (usuarioLogado == null) {
                            realizarCadastro();
                        } else {
                            System.out.println("Deslogue (Opção 9) para criar uma nova conta.");
                        }
                        break;
                    case 3:
                        listarProdutos();
                        break;
                    case 9:
                        if (usuarioLogado != null) {
                            this.usuarioLogado = null;
                            System.out.println("Logout realizado com sucesso.");
                        } else {
                            System.out.println("Você não está logado.");
                        }
                        break;
                    case 0:
                        this.usuarioLogado = null;
                        System.out.println("Encerrando FarmaAdmin. Até logo!");
                        break;
                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número.");
            }
        }
        scanner.close();
    }
}