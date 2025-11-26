package br.com.farmaadmin;

import br.com.farmaadmin.dao.ProdutoDAO;
import br.com.farmaadmin.dao.UsuarioDAO;
import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.modelo.Usuario;
import br.com.farmaadmin.util.DatabaseConfig;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

public class ProdutoInsertTest {

    @Test
    public void testInsertProductForFarmacia() throws Exception {
        // Cria usuário farmácia
        UsuarioDAO udao = new UsuarioDAO();
        Usuario u = new Usuario("Prod Test Farm", "prodtest@mail.local", "senha123", "FARMACIA");

        // Cleanup any previous residue for this test email
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

        ProdutoDAO pdao = new ProdutoDAO();
        Produto p = new Produto("TesteProd", "desc", 5.0, 10, created.getId(), "TestCat");
        Produto inserted = pdao.adicionar(p);
        assertNotNull(inserted);
        assertTrue(inserted.getId() > 0);

        // Verifica que o produto pertence à farmacia
        try (Connection conn = DatabaseConfig.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT farmacia_id FROM produto WHERE id = ?");
            ps.setInt(1, inserted.getId());
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            int fid = rs.getInt(1);
            assertEquals(created.getId(), fid);
        }

        // Cleanup: deletar o produto, farmacia e usuario criados
        try (Connection conn = DatabaseConfig.getConnection()) {
            PreparedStatement pd = conn.prepareStatement("DELETE FROM produto WHERE id = ?");
            pd.setInt(1, inserted.getId()); pd.executeUpdate();
            PreparedStatement fd = conn.prepareStatement("DELETE FROM farmacia WHERE id = ?");
            fd.setInt(1, created.getId()); fd.executeUpdate();
            PreparedStatement ud = conn.prepareStatement("DELETE FROM usuario WHERE id = ?");
            ud.setInt(1, created.getId()); ud.executeUpdate();
        }
    }
}
