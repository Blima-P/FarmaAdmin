package br.com.farmaadmin.controller;

import br.com.farmaadmin.dao.ProdutoDAO;
import br.com.farmaadmin.modelo.Produto;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;
import java.util.List;


@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        try {
            List<Produto> produtos = produtoDAO.listarTodos();
            return ResponseEntity.ok(produtos);
        } catch (SQLException e) {
            System.err.println("Erro SQL ao listar produtos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Produto> adicionar(@RequestBody Produto produto) {
        try {
            Produto novoProduto = produtoDAO.adicionar(produto);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
        } catch (SQLException e) {
            System.err.println("Erro SQL ao adicionar produto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable int id, @RequestBody Produto produto) {
        try {
            produto.setId(id);
            if (produtoDAO.atualizar(produto)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (SQLException e) {
            System.err.println("Erro SQL ao atualizar produto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable int id) {
        try {
            if (produtoDAO.deletar(id)) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (SQLException e) {
            System.err.println("Erro SQL ao deletar produto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
