package br.com.farmaadmin.modelo;

import java.time.LocalDateTime;

public class Pedido {
    private int id;
    private int usuarioId; // Cliente que fez o pedido
    private LocalDateTime dataPedido;
    private String status;
    private double valorTotal;
    private String enderecoEntrega;

    public Pedido() {
        this.dataPedido = LocalDateTime.now(); // Define a data/hora atual por padrão
        this.status = "PENDENTE"; // Define o status inicial como PENDENTE
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    @Override
    public String toString() {
        return "Pedido [ID=" + id + ", Cliente ID=" + usuarioId + ", Status=" + status + ", Total=" + valorTotal + "]";
    }
}