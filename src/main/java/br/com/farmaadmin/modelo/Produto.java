package br.com.farmaadmin.modelo;

public class Produto {
    private int id;
    private String nome;
    private String descricao;
    private double preco;
    private int estoque;
    private int farmaciaId;
    private String categoria;

    public Produto() {}

    public Produto(String nome, String descricao, double preco, int estoque, int farmaciaId, String categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.farmaciaId = farmaciaId;
        this.categoria = categoria;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }
    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }
    public int getFarmaciaId() { return farmaciaId; }
    public void setFarmaciaId(int farmaciaId) { this.farmaciaId = farmaciaId; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    @Override
    public String toString() {
        return "Produto [ID=" + id + ", Nome=" + nome + ", Preco=" + preco + ", Estoque=" + estoque + "]";
    }
}