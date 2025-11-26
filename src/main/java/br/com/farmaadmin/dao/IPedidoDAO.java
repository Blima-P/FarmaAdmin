package br.com.farmaadmin.dao;

import java.sql.SQLException;
import java.util.List;
import br.com.farmaadmin.modelo.Pedido;
import br.com.farmaadmin.modelo.Produto;

public interface IPedidoDAO {

    /**
     * Registra um novo pedido e seus itens de forma transacional,
     * realizando também a baixa de estoque.
     * @param pedido O objeto Pedido a ser registrado.
     * @param itensCarrinho A lista de Produtos no carrinho (onde o 'estoque' representa a 'quantidade' comprada).
     * @return O Pedido registrado com seu ID gerado, ou null em caso de falha.
     */
    Pedido registrarNovoPedido(Pedido pedido, List<Produto> itensCarrinho) throws SQLException;

    /**
     * Lista todos os pedidos feitos por um usuário (cliente).
     * @param usuarioId ID do cliente.
     * @return Lista de Pedidos.
     */
    List<Pedido> listarPedidosPorUsuario(int usuarioId) throws SQLException;

    /**
     * Busca um pedido pelo seu ID.
     * @param id ID do pedido.
     * @return O objeto Pedido ou null.
     */
    Pedido buscarPorId(int id) throws SQLException;

    /**
     * Atualiza o status de um pedido. Usado pela Farmácia.
     * @param pedidoId ID do pedido a ser atualizado.
     * @param novoStatus O novo status (e.g., "EM_PROCESSAMENTO", "ENVIADO").
     * @return true se a atualização foi bem-sucedida.
     */
    boolean atualizarStatus(int pedidoId, String novoStatus) throws SQLException;

    // NOTA: Os métodos 'listarItensDoPedido' e 'listarPedidosParaFarmacia'
    // não são obrigatórios nesta interface, pois são métodos auxiliares
    // implementados diretamente no PedidoDAO.
}
