package br.com.farmaadmin.dao;

import br.com.farmaadmin.modelo.ItemPedido;
import br.com.farmaadmin.modelo.Pedido;
import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.util.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO implements IPedidoDAO {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    // Método auxiliar para extrair Pedido do ResultSet
    private Pedido extrairPedido(ResultSet rs) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setId(rs.getInt("id"));
        pedido.setUsuarioId(rs.getInt("usuario_id"));
        pedido.setValorTotal(rs.getDouble("valor_total"));
        pedido.setEnderecoEntrega(rs.getString("endereco_entrega"));
        pedido.setStatus(rs.getString("status"));
        // Se a coluna 'data_pedido' for TIMESTAMP/DATETIME no BD, use getTimestamp
        Timestamp ts = rs.getTimestamp("data_pedido");
        if (ts != null) {
            pedido.setDataPedido(ts.toLocalDateTime());
        }
        return pedido;
    }

    // Método auxiliar para extrair ItemPedido do ResultSet (útil para a Farmácia)
    private ItemPedido extrairItemPedido(ResultSet rs) throws SQLException {
        ItemPedido item = new ItemPedido();
        item.setPedidoId(rs.getInt("pedido_id"));
        item.setProdutoId(rs.getInt("produto_id"));
        item.setNomeProduto(rs.getString("nome")); // nome é do produto
        item.setQuantidade(rs.getInt("quantidade"));
        item.setPrecoUnitario(rs.getDouble("preco_unitario"));
        item.setFarmaciaId(rs.getInt("farmacia_id")); // ID da farmácia do produto
        return item;
    }

    @Override
    public Pedido registrarNovoPedido(Pedido pedido, List<Produto> itensCarrinho) throws SQLException {
        String sqlPedido = "INSERT INTO pedido (usuario_id, valor_total, endereco_entrega, status) VALUES (?, ?, ?, ?)";
        String sqlItens = "INSERT INTO itens_pedido (pedido_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // 🚩 INICIA A TRANSAÇÃO (TUDO OU NADA)

            // 1. INSERIR PEDIDO PRINCIPAL
            try (PreparedStatement stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                stmtPedido.setInt(1, pedido.getUsuarioId());
                stmtPedido.setDouble(2, pedido.getValorTotal());
                stmtPedido.setString(3, pedido.getEnderecoEntrega());
                stmtPedido.setString(4, pedido.getStatus());

                if (stmtPedido.executeUpdate() == 0) throw new SQLException("Falha ao criar pedido principal.");

                try (ResultSet rs = stmtPedido.getGeneratedKeys()) {
                    if (rs.next()) {
                        pedido.setId(rs.getInt(1));
                    } else {
                        throw new SQLException("Falha ao obter ID do pedido.");
                    }
                }
            }

            // 2. INSERIR ITENS E DECREMENTAR ESTOQUE
            try (PreparedStatement stmtItens = conn.prepareStatement(sqlItens)) {
                for (Produto item : itensCarrinho) {

                    // A. TENTA BAIXAR O ESTOQUE
                    if (!produtoDAO.decrementarEstoque(item.getId(), item.getEstoque())) {
                        // Se decrementarEstoque retornar false (falha ou estoque insuficiente)
                        throw new SQLException("Estoque insuficiente para o produto ID: " + item.getId());
                    }

                    // B. INSERE O ITEM DO PEDIDO
                    stmtItens.setInt(1, pedido.getId());
                    stmtItens.setInt(2, item.getId());
                    stmtItens.setInt(3, item.getEstoque()); // Estoque = Quantidade
                    stmtItens.setDouble(4, item.getPreco());
                    stmtItens.addBatch();
                }
                stmtItens.executeBatch();
            }

            conn.commit(); // ✅ FINALIZA A TRANSAÇÃO COM SUCESSO
            return pedido;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // ❌ DESFAZ EM CASO DE ERRO (Reverte estoque e pedido)
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // Implementação de Listar Pedidos do Cliente
    @Override
    public List<Pedido> listarPedidosPorUsuario(int usuarioId) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM pedido WHERE usuario_id = ? ORDER BY data_pedido DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(extrairPedido(rs));
                }
            }
        }
        return pedidos;
    }

    // Implementação de Listar Itens do Pedido (Auxiliar para Cliente/Farmácia)
    public List<ItemPedido> listarItensDoPedido(int pedidoId) throws SQLException {
        List<ItemPedido> itens = new ArrayList<>();
        // Query que junta itens_pedido com a tabela produto para pegar o nome
        String sql = "SELECT ip.*, p.nome, p.farmacia_id FROM itens_pedido ip " +
                "JOIN produto p ON ip.produto_id = p.id WHERE ip.pedido_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pedidoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itens.add(extrairItemPedido(rs));
                }
            }
        }
        return itens;
    }

    // NOVO MÉTODO: Lista pedidos que contêm produtos de uma farmácia específica
    public List<Pedido> listarPedidosParaFarmacia(int farmaciaId) throws SQLException {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM pedido p " +
                "JOIN itens_pedido ip ON p.id = ip.pedido_id " +
                "JOIN produto pr ON ip.produto_id = pr.id " +
                "WHERE pr.farmacia_id = ? " +
                "ORDER BY p.data_pedido ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, farmaciaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(extrairPedido(rs));
                }
            }
        }
        return pedidos;
    }

    @Override
    public Pedido buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM pedido WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extrairPedido(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean atualizarStatus(int pedidoId, String novoStatus) throws SQLException {
        String sql = "UPDATE pedido SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoStatus);
            stmt.setInt(2, pedidoId);
            return stmt.executeUpdate() > 0;
        }
    }
}