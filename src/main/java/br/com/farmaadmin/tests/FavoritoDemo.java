package br.com.farmaadmin.tests;

import br.com.farmaadmin.dao.FavoritoDAO;
import br.com.farmaadmin.util.DatabaseConfig;
import br.com.farmaadmin.modelo.Produto;

import java.sql.*;
import java.util.List;

public class FavoritoDemo {
    public static void main(String[] args) {
        System.out.println("FavoritoDemo: demonstrando favoritar sem interação UI");
        String email = "cli_test@local";
        String senha = "test123";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            // ensure user exists
            int usuarioId = -1;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM usuario WHERE email = ?")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) { if (rs.next()) usuarioId = rs.getInt(1); }
            }
            if (usuarioId == -1) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO usuario (nome,email,senha,tipo_usuario) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Cliente Demo"); ps.setString(2, email); ps.setString(3, senha); ps.setString(4, "CLIENTE");
                    ps.executeUpdate(); try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) usuarioId = rs.getInt(1); }
                }
            }

            // ensure product exists
            int produtoId = -1;
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT id FROM produto LIMIT 1")) { if (rs.next()) produtoId = rs.getInt(1); }
            if (produtoId == -1) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO produto (nome,descricao,preco,estoque,categoria) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Produto Demo"); ps.setString(2, "Demo"); ps.setDouble(3, 9.9); ps.setInt(4, 10); ps.setString(5, "Demo");
                    ps.executeUpdate(); try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) produtoId = rs.getInt(1); }
                }
            }

            conn.commit();

            System.out.println("Usuário ID: " + usuarioId + " | Produto ID: " + produtoId);

            FavoritoDAO fav = new FavoritoDAO();
            boolean added = fav.adicionarFavorito(usuarioId, produtoId);
            System.out.println("Adicionado aos favoritos: " + added);

            List<Produto> favs = fav.listarProdutosFavoritosPorUsuario(usuarioId);
            System.out.println("Produtos nos favoritos do usuário:");
            for (Produto p : favs) System.out.println(" - " + p.getId() + " | " + p.getNome() + " | R$" + p.getPreco());

            boolean removed = fav.removerFavorito(usuarioId, produtoId);
            System.out.println("Removido dos favoritos: " + removed);

            System.exit(0);
        } catch (SQLException e) {
            System.err.println("Erro no demo: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
