package br.com.farmaadmin.dao;

import java.sql.*;
import java.util.Map;
import br.com.farmaadmin.util.DatabaseConfig;
import br.com.farmaadmin.modelo.Usuario;

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
    // Backwards-compatible method: delega para a nova assinatura sem campos adicionais
    public Usuario adicionar(Usuario usuario) throws SQLException {
        return adicionar(usuario, null);
    }

    /**
     * Cria um usuário e, se for do tipo FARMACIA, cria a linha correspondente em `farmacia` na mesma transação.
     * Aceita um mapa `farmaciaFields` com valores explícitos para colunas de `farmacia`.
     */
    public Usuario adicionar(Usuario usuario, java.util.Map<String, Object> farmaciaFields) throws SQLException {
        String sql = "INSERT INTO usuario (nome, email, senha, tipo_usuario) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DatabaseConfig.getConnection();
            // Transação: cria usuario e, se necessário, cria farmacia no mesmo connection
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getTipoUsuario());

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas == 0) {
                conn.rollback();
                return null;
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
            }

            // Se for FARMACIA, garanta a criação da linha em 'farmacia' usando mesma conexão
            if ("FARMACIA".equalsIgnoreCase(usuario.getTipoUsuario())) {
                boolean ok = FarmaciaDAO.ensureFarmaciaExists(conn, usuario.getId(), farmaciaFields);
                if (!ok) {
                    conn.rollback();
                    throw new SQLException("Falha ao criar registro em 'farmacia' para o novo usuário");
                }
            }

            conn.commit();
            usuario.setSenha("");
            return usuario;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            System.err.println("Erro SQL ao adicionar usuário: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException ex) { /* ignore */ }
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { /* ignore */ }
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
