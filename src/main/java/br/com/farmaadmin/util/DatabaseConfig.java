package br.com.farmaadmin.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    // Mantenha a porta 3306 a menos que seu MySQL use outra
    private static final String DB_URL = "jdbc:mysql://localhost:3306/farma_admin?useTimezone=true&serverTimezone=UTC";

    // Credenciais (mude se for usar outras no seu MySQL)
    private static final String DB_USER = "farma_user";
    private static final String DB_PASSWORD = "farma_senha123";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("ERRO: Driver JDBC não encontrado. Verifique se a dependência MySQL está no seu projeto.");
            throw new SQLException("Driver JDBC ausente.", e);
        }
        // Tenta estabelecer a conexão com o banco de dados
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (SQLException e) {
            System.err.println("❌ Erro de Conexão com o Banco de Dados (Verifique se o MySQL está ativo):");
            System.err.println("Causa: " + e.getMessage());
            return false;
        }
    }
}