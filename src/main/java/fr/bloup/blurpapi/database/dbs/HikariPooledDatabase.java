package fr.bloup.blurpapi.database.dbs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.bloup.blurpapi.database.Database;
import fr.bloup.blurpapi.database.ResultSetHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Savepoint;

public class HikariPooledDatabase implements Database {
    private final HikariDataSource ds;

    public HikariPooledDatabase(String driverClassName, String jdbcUrl, String user, String pass) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(pass);
        // réglages par défaut du pool (taille, timeout, etc.) peuvent être ajustés ici
        this.ds = new HikariDataSource(config);
    }

    @Override
    public void connect() {
        // pas nécessaire : le pool gère les connexions à la demande
    }

    @Override
    public void disconnect() {
        ds.close();
    }

    @Override
    public boolean isConnected() {
        // toujours disponible si le pool n'est pas fermé
        return !ds.isClosed();
    }

    @Override
    public int executeUpdate(String sql, Object... params) throws Exception {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }

    @Override
    public <T> T executeQuery(String sql, ResultSetHandler<T> handler, Object... params) throws Exception {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return handler.handle(rs);
            }
        }
    }

    @Override
    public void beginTransaction() {
        throw new UnsupportedOperationException("Transactions not supported with pooled connections");
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException("Transactions not supported with pooled connections");
    }

    @Override
    public void rollback() {
        throw new UnsupportedOperationException("Transactions not supported with pooled connections");
    }

    @Override
    public Savepoint setSavepoint(String name) throws Exception {
        throw new UnsupportedOperationException("Savepoints not supported with pooled connections");
    }

    @Override
    public void rollbackTo(Savepoint savepoint) throws Exception {
        throw new UnsupportedOperationException("Savepoints not supported with pooled connections");
    }
}
