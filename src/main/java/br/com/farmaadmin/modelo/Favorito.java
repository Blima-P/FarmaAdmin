package br.com.farmaadmin.modelo;

import java.time.LocalDateTime;

public class Favorito {
    private int id;
    private int usuarioId;
    private int produtoId;
    private LocalDateTime criadoEm;

    public Favorito() {}

    public Favorito(int usuarioId, int produtoId) {
        this.usuarioId = usuarioId;
        this.produtoId = produtoId;
        this.criadoEm = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
