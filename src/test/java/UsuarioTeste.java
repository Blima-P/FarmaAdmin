import br.com.farmaadmin.modelo.ItemPedido;
import br.com.farmaadmin.modelo.Usuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UsuarioTeste {

    @Test
    public void testeCadastroUsuario(){
        //Arrange
        Usuario usuario = new Usuario();

        //Act
        usuario.setId(1);
        usuario.setNome("Beatriz");
        usuario.setEmail("beatriz@gmail.com");
        usuario.setSenha("Hu6&*24");
        usuario.setTipoUsuario("Cliente");

        //Assert
        Assertions.assertEquals(1, usuario.getId());
        Assertions.assertEquals("Beatriz", usuario.getNome());
        Assertions.assertEquals("beatriz@gmail.com", usuario.getEmail());
        Assertions.assertEquals("Hu6&*24", usuario.getSenha());
        Assertions.assertEquals("Cliente", usuario.getTipoUsuario());
    }
}
