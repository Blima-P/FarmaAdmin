package br.com.farmaadmin.modelo;

public class Farmacia {
    private int id;
    private String nome;
    private String email;
    private String cnpj;
    private String telefone;
    private String endereco;
    private Integer usuarioId;

    public Farmacia() {}

    public Farmacia(int id, String nome, String email, String cnpj, String telefone, String endereco, Integer usuarioId) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.endereco = endereco;
        this.usuarioId = usuarioId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
}
