package br.com.farmaadmin;

import br.com.farmaadmin.dao.UsuarioDAO;
import br.com.farmaadmin.dao.FarmaciaDAO;
import br.com.farmaadmin.util.DatabaseConfig;
import br.com.farmaadmin.modelo.Usuario;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

public class FarmaciaTransactionTest {

    @Test
    public void testCreateUsuarioAndFarmaciaTransaction() throws Exception {
        UsuarioDAO udao = new UsuarioDAO();
        Usuario u = new Usuario("Test Farmacia", "testfarm@mail.local", "senha123", "FARMACIA");

        // Cleanup any previous test residue with same email
        try (Connection conn = DatabaseConfig.getConnection()) {
            PreparedStatement p0 = conn.prepareStatement("DELETE FROM produto WHERE farmacia_id = (SELECT id FROM usuario WHERE email = ?)");
            p0.setString(1, u.getEmail()); p0.executeUpdate();
            PreparedStatement p1 = conn.prepareStatement("DELETE FROM farmacia WHERE id = (SELECT id FROM usuario WHERE email = ?)");
            p1.setString(1, u.getEmail()); p1.executeUpdate();
            PreparedStatement p2 = conn.prepareStatement("DELETE FROM usuario WHERE email = ?");
            p2.setString(1, u.getEmail()); p2.executeUpdate();
        }

        Usuario created = udao.adicionar(u);
        assertNotNull(created);
        assertTrue(created.getId() > 0);

        // Verifica se farmacia existe
        try (Connection conn = DatabaseConfig.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM farmacia WHERE id = ?");
            ps.setInt(1, created.getId());
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next(), "Registro de farmacia deveria existir após cadastro");
        }

        // Cleanup: remover farmacia + usuario criado
        try (Connection conn = DatabaseConfig.getConnection()) {
            PreparedStatement d1 = conn.prepareStatement("DELETE FROM produto WHERE farmacia_id = ?");
            d1.setInt(1, created.getId()); d1.executeUpdate();
            PreparedStatement d2 = conn.prepareStatement("DELETE FROM farmacia WHERE id = ?");
            d2.setInt(1, created.getId()); d2.executeUpdate();
            PreparedStatement d3 = conn.prepareStatement("DELETE FROM usuario WHERE id = ?");
            d3.setInt(1, created.getId()); d3.executeUpdate();
        }
    }
}
