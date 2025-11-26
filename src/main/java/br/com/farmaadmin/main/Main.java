package br.com.farmaadmin.main;

import br.com.farmaadmin.util.DatabaseConfig;
import br.com.farmaadmin.dao.ProdutoDAO;
import br.com.farmaadmin.modelo.Produto;
import java.util.List;
import java.sql.SQLException;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

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
        // Support scripted mode: java -cp ... br.com.farmaadmin.main.Main --script input.txt
        MenuPrincipal menuPrincipal;
        if (args.length >= 2 && "--script".equals(args[0])) {
            String scriptPath = args[1];
            try (java.util.Scanner sc = new java.util.Scanner(new java.io.FileInputStream(scriptPath))) {
                menuPrincipal = new MenuPrincipal(sc);
                menuPrincipal.exibirMenu();
            } catch (java.io.FileNotFoundException e) {
                System.err.println("Script file not found: " + scriptPath);
                return;
            }
        } else {
            menuPrincipal = new MenuPrincipal();
            menuPrincipal.exibirMenu();
        }

        System.out.println("\n=============================================");
        System.out.println("        Fim da Execução Principal          ");
        System.out.println("=============================================");

        // Tenta limpar threads do driver MySQL ao finalizar (suaviza warning de shutdown)
        try {
            AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Throwable t) {
            // ignore se a classe não estiver disponível ou se a limpeza falhar
        }
    }
}
