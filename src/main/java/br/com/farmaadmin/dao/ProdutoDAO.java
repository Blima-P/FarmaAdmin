package br.com.farmaadmin.dao;

import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.util.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO implements IProdutoDAO {

    /**
     * Auxilia a extrair dados de um ResultSet e criar um objeto Produto.
     */
    private Produto extrairProduto(ResultSet rs) throws SQLException {
        Produto produto = new Produto();
        produto.setId(rs.getInt("id"));
        produto.setNome(rs.getString("nome"));
        produto.setDescricao(rs.getString("descricao"));
        produto.setPreco(rs.getDouble("preco"));
        produto.setEstoque(rs.getInt("estoque"));
        produto.setFarmaciaId(rs.getInt("farmacia_id"));
        produto.setCategoria(rs.getString("categoria"));
        return produto;
    }

    @Override
    public Produto adicionar(Produto produto) throws SQLException {
        String sql = "INSERT INTO produto (nome, descricao, preco, estoque, farmacia_id, categoria) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setDouble(3, produto.getPreco());
            stmt.setInt(4, produto.getEstoque());
            stmt.setInt(5, produto.getFarmaciaId());
            stmt.setString(6, produto.getCategoria());

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        produto.setId(rs.getInt(1));
                    }
                }
                return produto;
            }
            return null;

        } catch (SQLException e) {
            System.err.println("Erro SQL ao adicionar produto: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Produto> listarTodos() throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produto";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                produtos.add(extrairProduto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os produtos: " + e.getMessage());
            throw e;
        }
        return produtos;
    }

    @Override
    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produto WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extrairProduto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produto por ID: " + e.getMessage());
            throw e;
        }
        return null;
    }

    @Override
    public boolean atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE produto SET nome = ?, descricao = ?, preco = ?, estoque = ?, farmacia_id = ?, categoria = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setDouble(3, produto.getPreco());
            stmt.setInt(4, produto.getEstoque());
            stmt.setInt(5, produto.getFarmaciaId());
            stmt.setString(6, produto.getCategoria());
            stmt.setInt(7, produto.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM produto WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao deletar produto: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Implementa a lógica de baixa de estoque. É usada dentro da transação do PedidoDAO.
     * Garante que o estoque não fique negativo.
     * @return true se o estoque foi decrementado com sucesso, false caso contrário (ex: estoque insuficiente).
     */
    @Override
    public boolean decrementarEstoque(int produtoId, int quantidade) throws SQLException {
        // A condição "estoque >= ?" impede que o estoque caia abaixo de zero.
        String sql = "UPDATE produto SET estoque = estoque - ? WHERE id = ? AND estoque >= ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantidade);
            stmt.setInt(2, produtoId);
            stmt.setInt(3, quantidade);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao decrementar estoque: " + e.getMessage());
            throw e;
        }
    }
}