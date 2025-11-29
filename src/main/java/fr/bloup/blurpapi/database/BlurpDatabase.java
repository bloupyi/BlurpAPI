package fr.bloup.blurpapi.database;

import fr.bloup.blurpapi.database.dbs.*;

public class BlurpDatabase {
    private DatabaseType type;
    private String host;
    private int port;
    private String user;
    private String pass;
    private String databaseName;
    private String filePath;

    private boolean useSSL = false;
    private boolean allowPublicKeyRetrieval = false;

    public BlurpDatabase type(DatabaseType type) {
        this.type = type;
        return this;
    }

    public BlurpDatabase host(String host) {
        this.host = host;
        return this;
    }

    public BlurpDatabase port(int port) {
        this.port = port;
        return this;
    }

    public BlurpDatabase user(String user) {
        this.user = user;
        return this;
    }

    public BlurpDatabase password(String pass) {
        this.pass = pass;
        return this;
    }

    public BlurpDatabase database(String db) {
        this.databaseName = db;
        return this;
    }

    public BlurpDatabase file(String path) {
        this.filePath = path;
        return this;
    }

    public BlurpDatabase useSSL(boolean useSSL) {
        this.useSSL = useSSL;
        return this;
    }

    public BlurpDatabase allowPublicKeyRetrieval(boolean allowPublicKeyRetrieval) {
        this.allowPublicKeyRetrieval = allowPublicKeyRetrieval;
        return this;
    }

    public Database build() {
        switch (type) {
            case MYSQL:
                return new MySQLDatabase(host, port, databaseName, user == null ? "root" : user, pass == null ? "" : pass, useSSL, allowPublicKeyRetrieval);
            case SQLITE:
                return new SQLiteDatabase(filePath);
            case MARIADB:
                return new MariaDBDatabase(host, port, databaseName, user == null ? "root" : user, pass == null ? "" : pass, useSSL, allowPublicKeyRetrieval);
            case POSTGRESQL:
                return new PostgreSQLDatabase(host, port, databaseName, user == null ? "postgres" : user, pass == null ? "" : pass, useSSL, allowPublicKeyRetrieval);
            case SQLSERVER:
                return new SQLServerDatabase(host, port, databaseName, user == null ? "sa" : user, pass == null ? "" : pass, useSSL);
            case ORACLE:
                return new OracleDatabase(host, port, databaseName, user == null ? "system" : user, pass == null ? "oracle" : pass);
            case HSQLDB:
                return new HSQLDBDatabase(filePath, user == null ? "SA" : user, pass == null ? "" : pass);
            case H2:
                return new H2Database(filePath, user == null ? "sa" : user, pass == null ? "" : pass);
            case null:
                throw new IllegalArgumentException("DB type is null");
            default:
                throw new IllegalArgumentException("Unknown DB type");
        }
    }
}
