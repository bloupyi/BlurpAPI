package fr.bloup.blurpapi.database.dbs;

import fr.bloup.blurpapi.database.Database;
import fr.bloup.blurpapi.database.ResultSetHandler;

import java.sql.*;

public class MariaDBDatabase implements Database {
    private final String url;
    private final String user;
    private final String pass;
    private Connection connection;

    public MariaDBDatabase(String host, int port, String database,
                           String user, String pass,
                           boolean useSSL, boolean allowPublicKeyRetrieval) {
        this.url = "jdbc:mariadb://" + host + ":" + port + "/" + database
                + "?useSSL=" + useSSL
                + "&allowPublicKeyRetrieval=" + allowPublicKeyRetrieval;
        this.user = user;
        this.pass = pass;
    }

    @Override
    public void connect() throws Exception {
        if (isConnected()) return;
        Class.forName("org.mariadb.jdbc.Driver");
        connection = DriverManager.getConnection(url, user, pass);
    }

    @Override
    public void disconnect() throws Exception {
        if (isConnected()) {
            connection.close();
        }
    }

    @Override
    public boolean isConnected() throws Exception {
        return connection != null && !connection.isClosed();
    }

    @Override
    public void executeUpdate(String sql, Object... params) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        }
    }

    @Override
    public <T> T executeQuery(String sql, ResultSetHandler<T> handler, Object... params) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return handler.handle(rs);
            }
        }
    }

    @Override
    public void beginTransaction() throws Exception {
        connect();
        connection.setAutoCommit(false);
    }

    @Override
    public void commit() throws Exception {
        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
            connection.setAutoCommit(true);
        }
    }

    @Override
    public void rollback() throws Exception {
        if (connection != null && !connection.getAutoCommit()) {
            connection.rollback();
            connection.setAutoCommit(true);
        }
    }

    @Override
    public Savepoint setSavepoint(String name) throws Exception {
        return connection.setSavepoint(name);
    }

    @Override
    public void rollbackTo(Savepoint savepoint) throws Exception {
        connection.rollback(savepoint);
    }
}
