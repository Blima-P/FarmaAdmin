import br.com.farmaadmin.modelo.Produto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProdutoTeste {

    @Test
    public void testeCadastroDeProduto(){
        //Arrange
        Produto produto = new Produto();

        //Act
        produto.setId(1);
        produto.setNome("remedio");
        produto.setDescricao("Analgesico teste");
        produto.setPreco(5.50);
        produto.setEstoque(200);
        produto.setFarmaciaId(1);
        produto.setCategoria("farmaceuticos");

        //Assert
        Assertions.assertEquals(1, produto.getId());
        Assertions.assertEquals("remedio", produto.getNome());
        Assertions.assertEquals("Analgesico teste", produto.getDescricao());
        Assertions.assertEquals(5.50, produto.getPreco());
        Assertions.assertEquals(200, produto.getEstoque());
        Assertions.assertEquals(1, produto.getFarmaciaId());
        Assertions.assertEquals("farmaceuticos", produto.getCategoria());


    }
}
