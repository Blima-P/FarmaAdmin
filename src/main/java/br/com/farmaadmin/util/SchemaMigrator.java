package br.com.farmaadmin.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Performs minimal, targeted schema migrations required for the app to run.
 * Currently ensures `produto.preco` column exists.
 */
public final class SchemaMigrator {

    private SchemaMigrator() {}

    /**
     * Ensure core schema constraints required by the application.
     * Returns true if schema is valid or successfully migrated.
     */
    public static boolean ensureCoreSchema() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Ensure produto.preco exists
            if (!columnExists(conn, "produto", "preco")) {
                String alter = "ALTER TABLE produto ADD COLUMN preco DECIMAL(10,2) NOT NULL DEFAULT 0.00";
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(alter);
                    System.out.println("[MIGRATION] Adicionada coluna produto.preco (DECIMAL(10,2) DEFAULT 0.00)");
                }
            }

            // Ensure produto.estoque exists
            if (!columnExists(conn, "produto", "estoque")) {
                String alter = "ALTER TABLE produto ADD COLUMN estoque INT NOT NULL DEFAULT 0";
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(alter);
                    System.out.println("[MIGRATION] Adicionada coluna produto.estoque (INT DEFAULT 0)");
                }
            }

            // Ensure farmacia reference column exists (farmacia_id or farmacia)
            boolean hasFarmaciaId = columnExists(conn, "produto", "farmacia_id");
            boolean hasFarmacia = columnExists(conn, "produto", "farmacia");
            if (!hasFarmaciaId && !hasFarmacia) {
                String alter = "ALTER TABLE produto ADD COLUMN farmacia_id INT";
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(alter);
                    System.out.println("[MIGRATION] Adicionada coluna produto.farmacia_id (INT)");
                }
            }

            // Optional columns: descricao, categoria
            if (!columnExists(conn, "produto", "descricao")) {
                String alter = "ALTER TABLE produto ADD COLUMN descricao TEXT";
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(alter);
                    System.out.println("[MIGRATION] Adicionada coluna produto.descricao (TEXT)");
                }
            }
            if (!columnExists(conn, "produto", "categoria")) {
                String alter = "ALTER TABLE produto ADD COLUMN categoria VARCHAR(100)";
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(alter);
                    System.out.println("[MIGRATION] Adicionada coluna produto.categoria (VARCHAR(100))");
                }
            }

            return true;
        } catch (SQLException e) {
            System.err.println("[MIGRATION] Falha ao validar/aplicar schema: " + e.getMessage());
            return false;
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, table, column)) {
            return rs.next();
        }
    }
}
