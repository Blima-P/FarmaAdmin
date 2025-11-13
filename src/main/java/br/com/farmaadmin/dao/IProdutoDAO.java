package br.com.farmaadmin.dao;

import br.com.farmaadmin.modelo.Produto;
import java.sql.SQLException;
import java.util.List;

public interface IProdutoDAO {
    Produto adicionar(Produto produto) throws SQLException;
    Produto buscarPorId(int id) throws SQLException;
    List<Produto> listarTodos() throws SQLException;
    boolean atualizar(Produto produto) throws SQLException;
    boolean deletar(int id) throws SQLException;

    // NOVO MÉTODO
    boolean decrementarEstoque(int produtoId, int quantidade) throws SQLException;
}