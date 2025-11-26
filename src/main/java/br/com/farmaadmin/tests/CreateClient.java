package br.com.farmaadmin.tests;

import br.com.farmaadmin.util.DatabaseConfig;

import java.sql.*;

/**
 * Utility to create (or reuse) a CLIENTE user and ensure at least one product exists.
 * Prints: EMAIL PASSWORD PRODUTO_ID
 */
public class CreateClient {
    public static void main(String[] args) {
        String email = "cli_test@local";
        String senha = "test123";
        int produtoId = -1;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            // Check or create user
            String selUser = "SELECT id FROM usuario WHERE email = ?";
            int usuarioId = -1;
            try (PreparedStatement ps = conn.prepareStatement(selUser)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) usuarioId = rs.getInt(1);
                }
            }

            if (usuarioId == -1) {
                String ins = "INSERT INTO usuario (nome, email, senha, tipo_usuario) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Cliente Teste");
                    ps.setString(2, email);
                    ps.setString(3, senha);
                    ps.setString(4, "CLIENTE");
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) usuarioId = rs.getInt(1); }
                }
            }

            // Ensure at least one product exists; get its id or create one
            String selProd = "SELECT id FROM produto LIMIT 1";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(selProd)) {
                if (rs.next()) produtoId = rs.getInt(1);
            }
            if (produtoId == -1) {
                String insP = "INSERT INTO produto (nome, descricao, preco, estoque, categoria) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insP, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Produto Demo");
                    ps.setString(2, "Produto para demonstração");
                    ps.setDouble(3, 5.0);
                    ps.setInt(4, 50);
                    ps.setString(5, "Demo");
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) produtoId = rs.getInt(1); }
                }
            }

            conn.commit();

            System.out.println(email + " " + senha + " " + produtoId);
            System.exit(0);

        } catch (SQLException e) {
            System.err.println("Erro ao criar cliente/produto: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
