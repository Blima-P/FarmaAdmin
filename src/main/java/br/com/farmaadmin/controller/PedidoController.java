package br.com.farmaadmin.controller;

import br.com.farmaadmin.dao.PedidoDAO;
import br.com.farmaadmin.modelo.Pedido;
import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.modelo.ItemPedido;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @PostMapping
    public ResponseEntity<?> registrarPedido(@RequestBody Map<String, Object> pedidoRequest) {
        try {
            Pedido pedido = new Pedido();
            pedido.setUsuarioId((Integer) pedidoRequest.get("usuarioId"));
            pedido.setValorTotal(((Number) pedidoRequest.get("valorTotal")).doubleValue());
            pedido.setEnderecoEntrega((String) pedidoRequest.get("enderecoEntrega"));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itensData = (List<Map<String, Object>>) pedidoRequest.get("itensCarrinho");

            List<Produto> itensCarrinho = itensData.stream().map(itemMap -> {
                Produto p = new Produto();
                p.setId((Integer) itemMap.get("id"));
                p.setEstoque((Integer) itemMap.get("quantidade"));
                p.setPreco(((Number) itemMap.get("preco")).doubleValue());
                return p;
            }).toList();

            Pedido novoPedido = pedidoDAO.registrarNovoPedido(pedido, itensCarrinho);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoPedido);

        } catch (SQLException e) {
            System.err.println("Erro SQL ao registrar pedido: " + e.getMessage());
            if (e.getMessage().contains("Estoque insuficiente")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao registrar pedido: " + e.getMessage());
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Pedido>> listarPorUsuario(@PathVariable int usuarioId) {
        try {
            List<Pedido> pedidos = pedidoDAO.listarPedidosPorUsuario(usuarioId);
            return ResponseEntity.ok(pedidos);
        } catch (SQLException e) {
            System.err.println("Erro SQL ao listar pedidos do usuário: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{pedidoId}/itens")
    public ResponseEntity<List<ItemPedido>> listarItens(@PathVariable int pedidoId) {
        try {
            List<ItemPedido> itens = pedidoDAO.listarItensDoPedido(pedidoId);
            return ResponseEntity.ok(itens);
        } catch (SQLException e) {
            System.err.println("Erro SQL ao listar itens do pedido: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/farmacia/{farmaciaId}")
    public ResponseEntity<List<Pedido>> listarPedidosFarmacia(@PathVariable int farmaciaId) {
        try {
            List<Pedido> pedidos = pedidoDAO.listarPedidosParaFarmacia(farmaciaId);
            return ResponseEntity.ok(pedidos);
        } catch (SQLException e) {
            System.err.println("Erro SQL ao listar pedidos para a farmácia: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{pedidoId}/status")
    public ResponseEntity<Void> atualizarStatus(@PathVariable int pedidoId, @RequestBody Map<String, String> statusRequest) {
        String novoStatus = statusRequest.get("status");
        if (novoStatus == null || novoStatus.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            if (pedidoDAO.atualizarStatus(pedidoId, novoStatus)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (SQLException e) {
            System.err.println("Erro SQL ao atualizar status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
