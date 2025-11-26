package br.com.farmaadmin.dao;

import java.sql.*;
import java.util.*;
import br.com.farmaadmin.modelo.Produto;
import br.com.farmaadmin.util.DatabaseConfig;

public class ProdutoDAO implements IProdutoDAO {

    /**
     * Auxilia a extrair dados de um ResultSet e criar um objeto Produto.
     */
    private Produto extrairProduto(ResultSet rs) throws SQLException {
        Produto produto = new Produto();
        produto.setId(rs.getInt("id"));
        produto.setNome(rs.getString("nome"));
        produto.setDescricao(rs.getString("descricao"));
        produto.setPreco(rs.getDouble("preco"));
        produto.setEstoque(rs.getInt("estoque"));
        // Alguns esquemas usam 'farmacia_id', outros usam 'farmacia' — tente ambos
        try {
            produto.setFarmaciaId(rs.getInt("farmacia_id"));
        } catch (SQLException ex) {
            try {
                produto.setFarmaciaId(rs.getInt("farmacia"));
            } catch (SQLException ex2) {
                // coluna não existe; mantenha valor padrão 0
                produto.setFarmaciaId(0);
            }
        }
        produto.setCategoria(rs.getString("categoria"));
        return produto;
    }

    // Verifica se determinada coluna existe na tabela (case-insensitive)
    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet cols = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return cols.next();
        }
    }

    /**
     * Garante que exista um registro na tabela `farmacia` com o id fornecido.
     * Se não existir, tenta criar um registro mínimo usando dados da tabela `usuario`.
     */
    public static boolean ensureFarmaciaExists(int farmaciaId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Verifica existência
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM farmacia WHERE id = ?")) {
                check.setInt(1, farmaciaId);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) return true;
                }
            } catch (SQLException e) {
                // tabela 'farmacia' pode não existir ou não estar acessível
                System.err.println("Aviso: não foi possível verificar existência da tabela 'farmacia': " + e.getMessage());
                return false;
            }

            // Busca dados do usuario para preencher campos básicos
            String usuarioNome = null;
            String usuarioEmail = null;
            try (PreparedStatement psu = conn.prepareStatement("SELECT nome, email FROM usuario WHERE id = ?")) {
                psu.setInt(1, farmaciaId);
                try (ResultSet rsu = psu.executeQuery()) {
                    if (rsu.next()) {
                        usuarioNome = rsu.getString("nome");
                        usuarioEmail = rsu.getString("email");
                    }
                }
            }

            // Obtém metadados das colunas de 'farmacia'
            DatabaseMetaData md = conn.getMetaData();
            List<ColumnMeta> cols = new ArrayList<>();
            try (ResultSet colRs = md.getColumns(null, null, "farmacia", null)) {
                while (colRs.next()) {
                    String colName = colRs.getString("COLUMN_NAME");
                    int dataType = colRs.getInt("DATA_TYPE");
                    String isNullable = colRs.getString("IS_NULLABLE");
                    String colDef = colRs.getString("COLUMN_DEF");
                    cols.add(new ColumnMeta(colName, dataType, "YES".equalsIgnoreCase(isNullable), colDef));
                }
            }

            if (cols.isEmpty()) {
                System.err.println("Aviso: tabela 'farmacia' sem colunas detectadas. Não foi possível criar registro.");
                return false;
            }

            // Prepara mapa de valores a inserir
            Map<String, Object> values = new LinkedHashMap<>();
            for (ColumnMeta c : cols) {
                String name = c.name;
                if (name.equalsIgnoreCase("id")) {
                    values.put(name, farmaciaId);
                    continue;
                }
                // Se coluna permite NULL ou tem DEFAULT, podemos omitir
                if (c.isNullable || c.columnDefault != null) continue;

                // Tenta preencher com dados do usuario conforme nome da coluna
                if (name.toLowerCase().contains("nome") && usuarioNome != null) {
                    values.put(name, usuarioNome);
                    continue;
                }
                if (name.toLowerCase().contains("email") && usuarioEmail != null) {
                    values.put(name, usuarioEmail);
                    continue;
                }
                // Se a coluna refere-se a um vínculo com usuario (ex: usuario_id), coloque o id do usuário/farmácia
                if (name.toLowerCase().contains("usuario") || name.toLowerCase().endsWith("_usuario") || name.toLowerCase().endsWith("usuario_id") || name.toLowerCase().endsWith("_id") && name.toLowerCase().contains("usuario")) {
                    // só atribui se o usuário existir (buscamos acima)
                    if (usuarioNome != null || usuarioEmail != null) {
                        values.put(name, farmaciaId);
                        continue;
                    }
                }
                // Preencher com valores neutros conforme tipo
                switch (c.dataType) {
                    case java.sql.Types.INTEGER:
                    case java.sql.Types.BIGINT:
                    case java.sql.Types.SMALLINT:
                        values.put(name, 0);
                        break;
                    default:
                        values.put(name, "");
                }
            }

            if (values.isEmpty()) {
                // Nenhuma coluna obrigatória detectada para inserir — tente inserir apenas id
                try (PreparedStatement ins = conn.prepareStatement("INSERT INTO farmacia (id) VALUES (?)")) {
                    ins.setInt(1, farmaciaId);
                    ins.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    System.err.println("Falha ao inserir farmacia com apenas id: " + e.getMessage());
                    return false;
                }
            }

            // Monta SQL dinâmico
            StringJoiner colsJoin = new StringJoiner(", ");
            StringJoiner paramsJoin = new StringJoiner(", ");
            for (String k : values.keySet()) {
                colsJoin.add(k);
                paramsJoin.add("?");
            }

            String sql = "INSERT INTO farmacia (" + colsJoin.toString() + ") VALUES (" + paramsJoin.toString() + ")";
            try (PreparedStatement ins = conn.prepareStatement(sql)) {
                int idx = 1;
                for (Object v : values.values()) {
                    if (v instanceof Integer) ins.setInt(idx++, (Integer) v);
                    else ins.setString(idx++, String.valueOf(v));
                }
                ins.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("Falha ao criar registro em 'farmacia': " + e.getMessage());
                return false;
            }
        }
    }

    // Helper class para metadados de coluna
    private static class ColumnMeta {
        final String name;
        final int dataType;
        final boolean isNullable;
        final String columnDefault;

        ColumnMeta(String name, int dataType, boolean isNullable, String columnDefault) {
            this.name = name;
            this.dataType = dataType;
            this.isNullable = isNullable;
            this.columnDefault = columnDefault;
        }
    }

    @Override
    public Produto adicionar(Produto produto) throws SQLException {
        // Detectar qual coluna de farmácia existe no esquema ('farmacia_id' ou 'farmacia')
        try (Connection conn = DatabaseConfig.getConnection()) {
            String farmaciaCol = columnExists(conn, "produto", "farmacia_id") ? "farmacia_id" :
                    (columnExists(conn, "produto", "farmacia") ? "farmacia" : null);

            String sql;
            if (farmaciaCol != null) {
                // Antes de inserir, garanta que exista registro na tabela 'farmacia' para evitar violação de FK
                if (produto.getFarmaciaId() > 0) {
                    try {
                        FarmaciaDAO.ensureFarmaciaExists(produto.getFarmaciaId());
                    } catch (SQLException e) {
                        System.err.println("Aviso: falha ao garantir farmacia antes de inserir produto: " + e.getMessage());
                        // prosseguir e deixar o erro SQL propagar se houver restrição
                    }
                }

                sql = "INSERT INTO produto (nome, descricao, preco, estoque, " + farmaciaCol + ", categoria) VALUES (?, ?, ?, ?, ?, ?)";
            } else {
                // sem coluna de farmácia, insira sem ela
                sql = "INSERT INTO produto (nome, descricao, preco, estoque, categoria) VALUES (?, ?, ?, ?, ?)";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, produto.getNome());
                stmt.setString(2, produto.getDescricao());
                stmt.setDouble(3, produto.getPreco());
                stmt.setInt(4, produto.getEstoque());

                if (farmaciaCol != null) {
                    stmt.setInt(5, produto.getFarmaciaId());
                    stmt.setString(6, produto.getCategoria());
                } else {
                    stmt.setString(5, produto.getCategoria());
                }

                int linhasAfetadas = stmt.executeUpdate();

                if (linhasAfetadas > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            produto.setId(rs.getInt(1));
                        }
                    }
                    return produto;
                }
                return null;

            } catch (SQLException e) {
                System.err.println("Erro SQL ao adicionar produto: " + e.getMessage());
                throw e;
            }
        }
    }

    @Override
    public List<Produto> listarTodos() throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produto";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                produtos.add(extrairProduto(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os produtos: " + e.getMessage());
            throw e;
        }
        return produtos;
    }

    @Override
    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produto WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extrairProduto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produto por ID: " + e.getMessage());
            throw e;
        }
        return null;
    }

    @Override
    public boolean atualizar(Produto produto) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String farmaciaCol = columnExists(conn, "produto", "farmacia_id") ? "farmacia_id" :
                    (columnExists(conn, "produto", "farmacia") ? "farmacia" : null);

            String sql;
            if (farmaciaCol != null) {
                sql = "UPDATE produto SET nome = ?, descricao = ?, preco = ?, estoque = ?, " + farmaciaCol + " = ?, categoria = ? WHERE id = ?";
            } else {
                sql = "UPDATE produto SET nome = ?, descricao = ?, preco = ?, estoque = ?, categoria = ? WHERE id = ?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, produto.getNome());
                stmt.setString(2, produto.getDescricao());
                stmt.setDouble(3, produto.getPreco());
                stmt.setInt(4, produto.getEstoque());

                if (farmaciaCol != null) {
                    stmt.setInt(5, produto.getFarmaciaId());
                    stmt.setString(6, produto.getCategoria());
                    stmt.setInt(7, produto.getId());
                } else {
                    stmt.setString(5, produto.getCategoria());
                    stmt.setInt(6, produto.getId());
                }

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean deletar(int id) throws SQLException {
        String sql = "DELETE FROM produto WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao deletar produto: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Implementa a lógica de baixa de estoque. É usada dentro da transação do PedidoDAO.
     * Garante que o estoque não fique negativo.
     * @return true se o estoque foi decrementado com sucesso, false caso contrário (ex: estoque insuficiente).
     */
    @Override
    public boolean decrementarEstoque(int produtoId, int quantidade) throws SQLException {
        // A condição "estoque >= ?" impede que o estoque caia abaixo de zero.
        String sql = "UPDATE produto SET estoque = estoque - ? WHERE id = ? AND estoque >= ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantidade);
            stmt.setInt(2, produtoId);
            stmt.setInt(3, quantidade);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao decrementar estoque: " + e.getMessage());
            throw e;
        }
    }
}
