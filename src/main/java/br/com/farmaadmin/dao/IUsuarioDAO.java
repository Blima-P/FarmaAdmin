package br.com.farmaadmin.dao;

import java.sql.SQLException;
import java.util.List;
import br.com.farmaadmin.modelo.Usuario;

public interface IUsuarioDAO {
    Usuario adicionar(Usuario usuario) throws SQLException;
    Usuario buscarPorEmailESenha(String email, String senha) throws SQLException;
}
