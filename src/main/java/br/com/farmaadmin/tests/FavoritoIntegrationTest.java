package br.com.farmaadmin.tests;

import br.com.farmaadmin.dao.FavoritoDAO;
import br.com.farmaadmin.util.DatabaseConfig;
import br.com.farmaadmin.modelo.Produto;

import java.sql.*;
import java.util.List;

public class FavoritoIntegrationTest {

    public static void main(String[] args) {
        System.out.println("FavoritoIntegrationTest: iniciando");
        int usuarioId = -1;
        int produtoId = -1;
        boolean createdProduct = false;
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            // 1) Criar usuário temporário
            String email = "temp_user_" + System.currentTimeMillis() + "@test.local";
            String insertUser = "INSERT INTO usuario (nome, email, senha, tipo_usuario) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "Temp User");
                ps.setString(2, email);
                ps.setString(3, "senha123");
                ps.setString(4, "CLIENTE");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) usuarioId = rs.getInt(1);
                }
            }

            // 2) Obter um produto existente; se não houver, criar um
            String selProd = "SELECT id FROM produto LIMIT 1";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(selProd)) {
                if (rs.next()) {
                    produtoId = rs.getInt("id");
                }
            }

            if (produtoId == -1) {
                String insProd = "INSERT INTO produto (nome, descricao, preco, estoque, categoria) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insProd, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Produto Temp");
                    ps.setString(2, "Produto criado para teste");
                    ps.setDouble(3, 1.0);
                    ps.setInt(4, 10);
                    ps.setString(5, "Teste");
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) produtoId = rs.getInt(1);
                    }
                    createdProduct = true;
                }
            }

                conn.commit();

                // Garantir que a tabela 'favorito' exista (sem constraints FK para compatibilidade)
                String createFavTable = "CREATE TABLE IF NOT EXISTS favorito (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "usuario_id INT NOT NULL, " +
                    "produto_id INT NOT NULL, " +
                    "criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE KEY uk_usuario_produto (usuario_id, produto_id)" +
                    ")";
                try (Statement s = conn.createStatement()) { s.execute(createFavTable); }

                if (usuarioId <= 0 || produtoId <= 0) {
                System.err.println("Falha: não foi possível criar/obter usuário ou produto.");
                System.exit(2);
            }

            FavoritoDAO favDao = new FavoritoDAO();

            // 3) Adicionar favorito
            boolean added = favDao.adicionarFavorito(usuarioId, produtoId);
            System.out.println("Adicionar favorito -> " + added);

            // 4) Listar favoritos e verificar
            List<Produto> favs = favDao.listarProdutosFavoritosPorUsuario(usuarioId);
            boolean found = false;
            for (Produto p : favs) {
                if (p.getId() == produtoId) { found = true; break; }
            }
            System.out.println("Favorito presente na listagem? " + found);

            if (!added && !found) {
                System.err.println("Falha ao adicionar favorito e não encontrado na listagem.");
                System.exit(3);
            }

            // 5) Remover favorito
            boolean removed = favDao.removerFavorito(usuarioId, produtoId);
            System.out.println("Remover favorito -> " + removed);

            // 6) Cleanup: remover usuário (e produto se foi criado agora)
            try (PreparedStatement d = conn.prepareStatement("DELETE FROM usuario WHERE id = ?")) {
                d.setInt(1, usuarioId);
                d.executeUpdate();
            }
            if (createdProduct) {
                try (PreparedStatement d2 = conn.prepareStatement("DELETE FROM produto WHERE id = ?")) {
                    d2.setInt(1, produtoId);
                    d2.executeUpdate();
                }
            }

            System.out.println("Teste de integração Favoritos concluído com sucesso.");
            System.exit(0);

        } catch (SQLException e) {
            System.err.println("Erro de banco no teste de integração: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
