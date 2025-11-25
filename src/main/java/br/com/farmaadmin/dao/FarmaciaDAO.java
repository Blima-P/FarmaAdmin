package br.com.farmaadmin.dao;

import br.com.farmaadmin.util.DatabaseConfig;
import java.sql.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.farmaadmin.modelo.Farmacia;

/**
 * DAO responsável por operações na tabela `farmacia`.
 * Fornece métodos para garantir/criar registros mínimos necessários.
 */
public class FarmaciaDAO {

    private static final Logger logger = LoggerFactory.getLogger(FarmaciaDAO.class);

    /**
     * Garante que exista um registro em `farmacia` com o id fornecido.
     * Usa nova conexão (auto) e fecha-a ao final.
     */
    public static boolean ensureFarmaciaExists(int farmaciaId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return ensureFarmaciaExists(conn, farmaciaId);
        }
    }

    /**
     * Garante que exista um registro em `farmacia` com o id fornecido usando a conexão fornecida.
     * Não fecha a conexão.
     */
    public static boolean ensureFarmaciaExists(Connection conn, int farmaciaId) throws SQLException {
        return ensureFarmaciaExists(conn, farmaciaId, null);
    }

    /**
     * Garante que exista um registro em `farmacia` com o id fornecido usando a conexão fornecida.
     * Permite passar valores explícitos para preencher colunas obrigatórias.
     */
    public static boolean ensureFarmaciaExists(Connection conn, int farmaciaId, Map<String, Object> providedValues) throws SQLException {
        // Verifica existência
        try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM farmacia WHERE id = ?")) {
            check.setInt(1, farmaciaId);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) return true;
            }
        } catch (SQLException e) {
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
            logger.warn("Tabela 'farmacia' sem colunas detectadas. Não foi possível criar registro.");
            return false;
        }

        // Prepara mapa de valores a inserir
        Map<String, Object> values = new LinkedHashMap<>();
        if (providedValues != null) {
            // normalize keys to lower-case for matching
            for (Map.Entry<String, Object> e : providedValues.entrySet()) {
                values.put(e.getKey(), e.getValue());
            }
        }
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

            // Se já foi fornecido explicitamente, respeite o valor fornecido
            if (values.containsKey(name)) continue;

            // Se a coluna refere-se a um vínculo com usuario (ex: usuario_id), coloque o id do usuário/farmácia
            if (name.toLowerCase().contains("usuario") || name.toLowerCase().endsWith("_usuario") || name.toLowerCase().endsWith("usuario_id") || (name.toLowerCase().endsWith("_id") && name.toLowerCase().contains("usuario"))) {
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
                logger.error("Falha ao inserir farmacia com apenas id: {}", e.getMessage());
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
            logger.error("Falha ao criar registro em 'farmacia': {}", e.getMessage());
            return false;
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

    // ---- CRUD básico para Farmacia (opcionalmente usado em outras partes) ----
    public Farmacia buscarPorId(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM farmacia WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Farmacia f = new Farmacia();
                    f.setId(rs.getInt("id"));
                    try { f.setNome(rs.getString("nome")); } catch (SQLException ignored) {}
                    try { f.setEmail(rs.getString("email")); } catch (SQLException ignored) {}
                    try { f.setCnpj(rs.getString("cnpj")); } catch (SQLException ignored) {}
                    try { f.setTelefone(rs.getString("telefone")); } catch (SQLException ignored) {}
                    try { f.setEndereco(rs.getString("endereco")); } catch (SQLException ignored) {}
                    try { f.setUsuarioId(rs.getInt("usuario_id")); } catch (SQLException ignored) {}
                    return f;
                }
            }
        }
        return null;
    }

    public boolean adicionar(Farmacia f) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            Map<String, Object> map = new LinkedHashMap<>();
            if (f.getCnpj() != null) map.put("cnpj", f.getCnpj());
            if (f.getTelefone() != null) map.put("telefone", f.getTelefone());
            if (f.getEndereco() != null) map.put("endereco", f.getEndereco());
            if (f.getUsuarioId() != null) map.put("usuario_id", f.getUsuarioId());
            return ensureFarmaciaExists(conn, f.getId(), map);
        }
    }

    public boolean deletar(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM farmacia WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean atualizar(Farmacia f) throws SQLException {
        // Atualização simples: atualiza colunas conhecidas se existirem
        try (Connection conn = DatabaseConfig.getConnection()) {
            StringBuilder sb = new StringBuilder();
            List<Object> params = new ArrayList<>();
            if (f.getNome() != null) { sb.append("nome = ?, "); params.add(f.getNome()); }
            if (f.getEmail() != null) { sb.append("email = ?, "); params.add(f.getEmail()); }
            if (f.getCnpj() != null) { sb.append("cnpj = ?, "); params.add(f.getCnpj()); }
            if (f.getTelefone() != null) { sb.append("telefone = ?, "); params.add(f.getTelefone()); }
            if (f.getEndereco() != null) { sb.append("endereco = ?, "); params.add(f.getEndereco()); }
            if (params.isEmpty()) return false;
            String setClause = sb.toString();
            if (setClause.endsWith(", ")) setClause = setClause.substring(0, setClause.length()-2);
            String sql = "UPDATE farmacia SET " + setClause + " WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int idx = 1;
                for (Object o : params) ps.setObject(idx++, o);
                ps.setInt(idx, f.getId());
                return ps.executeUpdate() > 0;
            }
        }
    }
}

