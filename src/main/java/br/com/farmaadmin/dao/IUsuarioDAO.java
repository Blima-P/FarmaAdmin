package br.com.farmaadmin.dao;

import br.com.farmaadmin.modelo.Usuario;
import java.sql.SQLException;

public interface IUsuarioDAO {
    Usuario adicionar(Usuario usuario) throws SQLException;
    Usuario buscarPorEmailESenha(String email, String senha) throws SQLException;
}