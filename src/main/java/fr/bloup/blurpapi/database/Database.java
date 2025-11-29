package fr.bloup.blurpapi.database;

import java.sql.Savepoint;

public interface Database {
    void connect() throws Exception;

    void disconnect() throws Exception;

    boolean isConnected() throws Exception;

    int executeUpdate(String sql, Object... params) throws Exception;

    <T> T executeQuery(String sql, ResultSetHandler<T> handler, Object... params) throws Exception;

    default DBQuery fromTable(String table) {
        return new DBQuery(this, table);
    }

    default TableBuilder createTable(String name) {
        return new TableBuilder(this, name);
    }

    void beginTransaction() throws Exception;
    void commit() throws Exception;
    void rollback() throws Exception;

    Savepoint setSavepoint(String name) throws Exception;

    void rollbackTo(Savepoint savepoint) throws Exception;
}
