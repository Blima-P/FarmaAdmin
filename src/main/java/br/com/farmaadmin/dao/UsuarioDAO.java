package br.com.farmaadmin.dao;

import br.com.farmaadmin.modelo.Usuario;
import br.com.farmaadmin.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioDAO implements IUsuarioDAO {

    private Usuario extrairUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setTipoUsuario(rs.getString("tipo_usuario"));
        return usuario;
    }

    @Override
    public Usuario adicionar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nome, email, senha, tipo_usuario) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getTipoUsuario());

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setId(rs.getInt(1));
                    }
                }
                usuario.setSenha("");
                return usuario;
            }
            return null;

        } catch (SQLException e) {
            System.err.println("Erro SQL ao adicionar usuário: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Usuario buscarPorEmailESenha(String email, String senha) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE email = ? AND senha = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = extrairUsuario(rs);
                    usuario.setSenha("");
                    return usuario;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL no login: " + e.getMessage());
            throw e;
        }
        return null;
    }
}