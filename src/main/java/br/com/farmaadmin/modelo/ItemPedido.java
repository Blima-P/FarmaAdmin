package br.com.farmaadmin.modelo;

public class ItemPedido {
    private int id;
    private int pedidoId;
    private int produtoId;
    private String nomeProduto; // Adicionado para facilitar a exibição
    private int quantidade;
    private double precoUnitario;
    private int farmaciaId; // Adicionado para facilitar o filtro da Farmácia

    // Construtores, Getters e Setters
    public ItemPedido() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }
    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }
    public int getFarmaciaId() { return farmaciaId; }
    public void setFarmaciaId(int farmaciaId) { this.farmaciaId = farmaciaId; }

    public double getSubtotal() {
        return precoUnitario * quantidade;
    }
}