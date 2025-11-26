package br.com.farmaadmin.main;

import java.util.Scanner;
import br.com.farmaadmin.dao.UsuarioDAO;
import br.com.farmaadmin.dao.ProdutoDAO;
import br.com.farmaadmin.modelo.Usuario;
import br.com.farmaadmin.modelo.Produto;
import java.util.List;
import java.sql.SQLException;

public class MenuPrincipal {

    private final Scanner scanner;
    private final UsuarioDAO usuarioDAO;
    private final ProdutoDAO produtoDAO;
    private Usuario usuarioLogado;

    public MenuPrincipal() {
        this(new Scanner(System.in));
    }

    // Overloaded constructor to allow scripted input (pass a Scanner)
    public MenuPrincipal(Scanner scanner) {
        this.scanner = scanner;
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
                    MenuFarmacia menuFarmacia = new MenuFarmacia(usuario, this.scanner);
                    menuFarmacia.exibirMenu();
                } else if ("CLIENTE".equals(usuario.getTipoUsuario())) {
                    // Chamada do novo Menu Cliente (usa o mesmo Scanner para modo script)
                    MenuCliente menuCliente = new MenuCliente(usuario, this.scanner);
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

        // Campos opcionais para Farmácia (po ssuem mais chance de serem NOT NULL em esquemas personalizados)
        System.out.print("CNPJ (opcional): ");
        String cnpj = scanner.nextLine();
        System.out.print("Telefone (opcional): ");
        String telefone = scanner.nextLine();
        System.out.print("Endereço (opcional): ");
        String endereco = scanner.nextLine();

        java.util.Map<String, Object> farmaciaFields = new java.util.HashMap<>();
        if (!cnpj.isBlank()) farmaciaFields.put("cnpj", cnpj);
        if (!telefone.isBlank()) farmaciaFields.put("telefone", telefone);
        if (!endereco.isBlank()) farmaciaFields.put("endereco", endereco);

        try {
            novo = usuarioDAO.adicionar(novo, farmaciaFields);
            if (novo != null) {
                System.out.println("✅ Cadastro realizado com sucesso! ID: " + novo.getId());
            } else {
                System.out.println("❌ Falha no cadastro. Verifique os dados.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao realizar cadastro (E-mail já pode estar em uso ou falha na criação da farmácia): " + e.getMessage());
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
