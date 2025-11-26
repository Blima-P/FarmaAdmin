package br.com.farmaadmin.dao;

import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.modelo.Favorito;
import br.com.farmaadmin.util.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoritoDAO {

    /**
     * Adiciona um favorito para um usuário. Retorna true se criado.
     */
    public boolean adicionarFavorito(int usuarioId, int produtoId) throws SQLException {
        String sql = "INSERT INTO favorito (usuario_id, produto_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, produtoId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            // Se violação de chave (já existe), apenas retorne false
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                return false;
            }
            throw e;
        }
    }

    public boolean removerFavorito(int usuarioId, int produtoId) throws SQLException {
        String sql = "DELETE FROM favorito WHERE usuario_id = ? AND produto_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, produtoId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Lista os produtos favoritados por um usuário. Faz JOIN com produto para retornar objetos Produto completos.
     */
    public List<Produto> listarProdutosFavoritosPorUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT p.* FROM produto p INNER JOIN favorito f ON p.id = f.produto_id WHERE f.usuario_id = ?";
        List<Produto> resultados = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto p = new Produto();
                    p.setId(rs.getInt("id"));
                    p.setNome(rs.getString("nome"));
                    p.setDescricao(rs.getString("descricao"));
                    p.setPreco(rs.getDouble("preco"));
                    try { p.setEstoque(rs.getInt("estoque")); } catch (SQLException ex) { p.setEstoque(0); }
                    try { p.setFarmaciaId(rs.getInt("farmacia_id")); } catch (SQLException ex) {
                        try { p.setFarmaciaId(rs.getInt("farmacia")); } catch (SQLException ex2) { p.setFarmaciaId(0); }
                    }
                    p.setCategoria(rs.getString("categoria"));
                    resultados.add(p);
                }
            }
        }
        return resultados;
    }
}
