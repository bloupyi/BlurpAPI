package fr.bloup.blurpapi.database;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TableBuilder {
    private final Database db;
    private final String tableName;
    private final List<Column> columns = new ArrayList<>();

    public TableBuilder(Database db, String tableName) {
        this.db = db;
        this.tableName = tableName;
    }

    public TableBuilder addColumn(String name, Class<?> type, ColumnOption... options) {
        columns.add(new Column(name, type, options));
        return this;
    }

    public void build() throws Exception {
        if (columns.isEmpty()) throw new IllegalStateException("No columns defined");

        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (\n" +
                columns.stream()
                        .map(this::columnToSQL)
                        .collect(Collectors.joining(",\n")) +
                "\n);";

        db.executeUpdate(sql);
    }

    public void alterAddColumn(String name, Class<?> type, ColumnOption... options) throws Exception {
        Column col = new Column(name, type, options);
        String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnToSQL(col);
        db.executeUpdate(sql);
    }

    public void alterDropColumn(String name) throws Exception {
        String sql = "ALTER TABLE " + tableName + " DROP COLUMN " + name;
        db.executeUpdate(sql);
    }

    private String columnToSQL(Column col) {
        StringBuilder sb = new StringBuilder();
        sb.append(col.getName())
                .append(" ")
                .append(javaTypeToSQL(col.getType()));

        if (col.getOptions().contains(ColumnOption.PRIMARY_KEY)) sb.append(" PRIMARY KEY");
        if (col.getOptions().contains(ColumnOption.UNIQUE)) sb.append(" UNIQUE");
        if (col.getOptions().contains(ColumnOption.NOT_NULL)) sb.append(" NOT NULL");
        if (col.getOptions().contains(ColumnOption.AUTO_INCREMENT)) sb.append(" AUTO_INCREMENT");

        return sb.toString();
    }

    private String javaTypeToSQL(Class<?> type) {
        if (type == String.class) return "VARCHAR(255)";
        if (type == Integer.class || type == int.class) return "INT";
        if (type == Long.class || type == long.class) return "BIGINT";
        if (type == Double.class || type == double.class) return "DOUBLE";
        if (type == Float.class || type == float.class) return "FLOAT";
        if (type == Boolean.class || type == boolean.class) return "BOOLEAN";
        if (type == UUID.class) return "CHAR(36)";
        if (type == byte[].class) return "BLOB";
        if (type == Date.class) return "DATE";
        if (type == Time.class) return "TIME";
        if (type == Timestamp.class) return "TIMESTAMP";
        if (type == BigDecimal.class) return "DECIMAL";
        if (type == Short.class || type == short.class) return "SMALLINT";
        if (type == Byte.class || type == byte.class) return "TINYINT";
        if (type == Character.class || type == char.class) return "CHAR";
        return "TEXT"; // fallback
    }
}
