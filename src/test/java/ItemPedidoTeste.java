import br.com.farmaadmin.modelo.ItemPedido;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ItemPedidoTeste {

    @Test
    public void cadastrarItemPedido(){
        //Arrange
        ItemPedido itemPedido = new ItemPedido();

        //Act
        itemPedido.setId(1);
        itemPedido.setPedidoId(1234);
        itemPedido.setProdutoId(23);
        itemPedido.setNomeProduto("dramin");
        itemPedido.setQuantidade(4);
        itemPedido.setPrecoUnitario(12.50);
        itemPedido.setFarmaciaId(10);


        //Assert
        Assertions.assertEquals(1, itemPedido.getId());
        Assertions.assertEquals(1234, itemPedido.getPedidoId());
        Assertions.assertEquals(23, itemPedido.getProdutoId());
        Assertions.assertEquals("dramin", itemPedido.getNomeProduto());
        Assertions.assertEquals(4, itemPedido.getQuantidade());
        Assertions.assertEquals(12.50, itemPedido.getPrecoUnitario());
        Assertions.assertEquals(10, itemPedido.getFarmaciaId());
    }

    @Test
    public void calcularSubtotal(){
        //Arrange
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setPrecoUnitario(3.50);
        itemPedido.setQuantidade(3);

        //Act
        double subtotal = itemPedido.getSubtotal();
        System.out.println("Subtotal: " + subtotal);

        //Assert
        Assertions.assertEquals(10.50, subtotal);
    }
}
