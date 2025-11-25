package br.com.farmaadmin.main;

import br.com.farmaadmin.dao.ProdutoDAO;
import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.util.DatabaseConfig;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("  💊 FarmaAdmin - Marketplace de Farmácias  ");
        System.out.println("=============================================");

        // 1. Teste de Conexão com o Banco de Dados
        System.out.print("\n[INIT] Testando conexão com o Banco de Dados... ");
        if (DatabaseConfig.testConnection()) {
            System.out.println("✅ OK! (Credenciais: farma_user/farma_senha123)");
        } else {
            System.err.println("❌ FALHA! O projeto será encerrado. Verifique se o MySQL está ativo, se o banco 'farma_admin' existe e se as credenciais no DatabaseConfig.java estão corretas.");
            return;
        }

        // 2. Demonstração de CRUD (Camada DAO)
        System.out.println("\n[CRUD TESTE] Testando a integridade do ProdutoDAO...");
        ProdutoDAO produtoDao = new ProdutoDAO();

        try {
            // Teste de Listagem (READ)
            List<Produto> produtosIniciais = produtoDao.listarTodos();
            System.out.println("A. Produtos iniciais no BD: " + produtosIniciais.size() + " encontrados.");

            // Teste de Criação (CREATE)
            // IMPORTANTE: Utiliza o farmacia_id 1 (criado no SQL)
            Produto novoProduto = new Produto("Teste - Deletar", "Produto de teste para CRUD", 9.99, 10, 1, "Teste");
            novoProduto = produtoDao.adicionar(novoProduto);

            if (novoProduto != null) {
                System.out.println("B. Produto CRIADO: " + novoProduto.getNome() + " (ID: " + novoProduto.getId() + ")");

                // Teste de Baixa de Estoque (NOVO FLUXO)
                boolean baixaEstoque = produtoDao.decrementarEstoque(novoProduto.getId(), 5);
                System.out.println("C. Estoque decrementado (5 unidades): " + (baixaEstoque ? "✅ SUCESSO" : "❌ FALHA"));

                // Teste de Deletar (DELETE)
                boolean deletado = produtoDao.deletar(novoProduto.getId());
                if (deletado) {
                    System.out.println("D. Produto DELETADO com sucesso. (ID: " + novoProduto.getId() + ")");
                }
            }


        } catch (SQLException e) {
            System.err.println("\n❌ ERRO FATAL no Teste DAO. As tabelas ou a conexão falharam. Certifique-se de ter executado o script SQL final.");
            System.err.println("Detalhes: " + e.getMessage());
            return;
        }

        // 3. Iniciar o fluxo da aplicação: Menu Principal
        System.out.println("\n=============================================");
        System.out.println("      INICIANDO FLUXO DE MENUS (MAIN)      ");
        System.out.println("=============================================");
        MenuPrincipal menuPrincipal = new MenuPrincipal();
        menuPrincipal.exibirMenu();

        System.out.println("\n=============================================");
        System.out.println("        Fim da Execução Principal          ");
        System.out.println("=============================================");
    }
}