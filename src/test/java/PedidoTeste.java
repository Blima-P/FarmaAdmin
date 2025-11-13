import br.com.farmaadmin.modelo.Pedido;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

public class PedidoTeste {

    @Test
    public void testeCadastroDePedido(){
        //Arrange
        Pedido pedido = new Pedido();

        //Act
        pedido.setId(2);
        pedido.setUsuarioId(1);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus("PENDENTE");
        pedido.setValorTotal(100.00);
        pedido.setEnderecoEntrega("Rua da alvorada número 5");

        //Assert
        Assertions.assertEquals(2,pedido.getId());
        Assertions.assertEquals(1,pedido.getUsuarioId());
        Assertions.assertEquals("PENDENTE",pedido.getStatus());
        Assertions.assertEquals(100.00,pedido.getValorTotal());
        Assertions.assertEquals("Rua da alvorada número 5", pedido.getEnderecoEntrega());

    }
}
