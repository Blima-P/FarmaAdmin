package br.com.farmaadmin.dao;

import java.sql.SQLException;
import java.util.List;
import br.com.farmaadmin.modelo.Produto;

public interface IProdutoDAO {
    Produto adicionar(Produto produto) throws SQLException;
    Produto buscarPorId(int id) throws SQLException;
    List<Produto> listarTodos() throws SQLException;
    boolean atualizar(Produto produto) throws SQLException;
    boolean deletar(int id) throws SQLException;

    // NOVO MÉTODO
    boolean decrementarEstoque(int produtoId, int quantidade) throws SQLException;
}
