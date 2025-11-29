package fr.bloup.blurpapi.database;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DBQuery {
    private final Database db;
    private final String table;
    private final List<String> joins = new ArrayList<>();
    private final List<String> whereCols = new ArrayList<>();
    private final List<Object> whereVals = new ArrayList<>();

    private String orderByClause = "";
    private Integer limitVal = null;
    private Integer offsetVal = null;

    public DBQuery(Database db, String table) {
        this.db = db;
        this.table = table;
    }

    public DBQuery join(String joinTable, String onCondition) {
        joins.add("JOIN " + joinTable + " ON " + onCondition);
        return this;
    }

    /**
     * Ajoute une condition WHERE column = value (chaînée avec AND).
     */
    public DBQuery where(String column, Object value) {
        whereCols.clear();
        whereVals.clear();
        whereCols.add(column);
        whereVals.add(value);
        return this;
    }

    /**
     * Ajoute une condition AND column = value.
     */
    public DBQuery andWhere(String column, Object value) {
        whereCols.add(column);
        whereVals.add(value);
        return this;
    }

    public DBQuery orderBy(String column, boolean asc) {
        this.orderByClause = "ORDER BY " + column + (asc ? " ASC" : " DESC");
        return this;
    }

    public DBQuery limit(int n) {
        this.limitVal = n;
        return this;
    }

    public DBQuery offset(int n) {
        this.offsetVal = n;
        return this;
    }

    private String buildJoin() {
        return joins.isEmpty() ? "" : " " + String.join(" ", joins);
    }

    private String buildWhere() {
        if (whereCols.isEmpty()) return "";
        return "WHERE " +
                IntStream.range(0, whereCols.size())
                        .mapToObj(i -> whereCols.get(i) + "=?")
                        .collect(Collectors.joining(" AND "));
    }

    private String buildClauses() {
        StringBuilder sb = new StringBuilder();
        String w = buildWhere();
        if (!w.isEmpty()) sb.append(" ").append(w);
        if (!orderByClause.isBlank()) sb.append(" ").append(orderByClause);
        if (limitVal != null) sb.append(" LIMIT ").append(limitVal);
        if (offsetVal != null) sb.append(" OFFSET ").append(offsetVal);
        return sb.toString();
    }

    /* ------- DELETE ------- */
    /**
     * Supprime les enregistrements selon JOIN/WHERE/ORDER/LIMIT/OFFSET.
     */
    public void delete() throws Exception {
        String sql = "DELETE FROM " + table + buildJoin() + buildClauses();
        db.executeUpdate(sql, whereVals.toArray());
    }

    /* -------------------- SETTERS -------------------- */

    public DBQuery set(String column, Object value) throws Exception {
        String sql = "UPDATE " + table +
                " SET " + column + "=? " +
                buildClauses();
        Object[] params = new Object[1 + whereVals.size()];
        params[0] = value;
        for (int i = 0; i < whereVals.size(); i++) {
            params[i + 1] = whereVals.get(i);
        }
        db.executeUpdate(sql, params);
        return this;
    }

    public DBQuery insert(Map<String, Object> values) throws Exception {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("insert values cannot be empty");
        }
        String cols = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream()
                .map(k -> "?")
                .collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + placeholders + ")";
        db.executeUpdate(sql, values.values().toArray());
        return this;
    }

    public DBQuery upsertOnDuplicateKey(Map<String, Object> values, String... updateColumns) throws Exception {
        if (values.isEmpty() || updateColumns.length == 0) {
            throw new IllegalArgumentException("upsert requires at least one column/value and one updateColumn");
        }
        // INSERT part
        String cols = String.join(", ", values.keySet());
        String placeholders = values.keySet().stream()
                .map(k -> "?")
                .collect(Collectors.joining(", "));
        // UPDATE part
        String updateClause = Arrays.stream(updateColumns)
                .map(col -> col + "=VALUES(" + col + ")")
                .collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + table +
                " (" + cols + ") VALUES (" + placeholders + ")" +
                " ON DUPLICATE KEY UPDATE " + updateClause;
        db.executeUpdate(sql, values.values().toArray());
        return this;
    }

    /* -------------------- GETTERS -------------------- */

    public String getString(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getString(column) : null,
                whereVals.toArray()
        );
    }

    public Integer getInt(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getInt(column) : null,
                whereVals.toArray()
        );
    }

    public Boolean getBoolean(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getBoolean(column) : null,
                whereVals.toArray()
        );
    }

    public Double getDouble(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getDouble(column) : null,
                whereVals.toArray()
        );
    }

    public Float getFloat(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getFloat(column) : null,
                whereVals.toArray()
        );
    }

    public Long getLong(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getLong(column) : null,
                whereVals.toArray()
        );
    }

    public Short getShort(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getShort(column) : null,
                whereVals.toArray()
        );
    }

    public Byte getByte(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getByte(column) : null,
                whereVals.toArray()
        );
    }

    public BigDecimal getBigDecimal(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getBigDecimal(column) : null,
                whereVals.toArray()
        );
    }

    public Date getDate(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getDate(column) : null,
                whereVals.toArray()
        );
    }

    public Time getTime(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getTime(column) : null,
                whereVals.toArray()
        );
    }

    public Timestamp getTimestamp(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getTimestamp(column) : null,
                whereVals.toArray()
        );
    }

    public Blob getBlob(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getBlob(column) : null,
                whereVals.toArray()
        );
    }

    public Clob getClob(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getClob(column) : null,
                whereVals.toArray()
        );
    }

    public Array getArray(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getArray(column) : null,
                whereVals.toArray()
        );
    }

    public Ref getRef(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getRef(column) : null,
                whereVals.toArray()
        );
    }

    public InputStream getAsciiStream(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getAsciiStream(column) : null,
                whereVals.toArray()
        );
    }

    public InputStream getBinaryStream(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getBinaryStream(column) : null,
                whereVals.toArray()
        );
    }

    public Object getObject(String column) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getObject(column) : null,
                whereVals.toArray()
        );
    }

    public <T> T getObject(String column, Class<T> type) throws Exception {
        String sql = "SELECT " + column + " FROM " + table + buildClauses();
        return db.executeQuery(
                sql,
                rs -> rs.next() ? rs.getObject(column, type) : null,
                whereVals.toArray()
        );
    }

    public <T> List<T> getList(RowMapper<T> mapper) throws Exception {
        String sql = "SELECT * FROM " + table + buildClauses();
        return db.executeQuery(sql, rs -> {
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapper.map(rs));
            }
            return list;
        }, whereVals.toArray());
    }
}
