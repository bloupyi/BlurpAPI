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
    private boolean autoReconnect = false;
    private boolean pool = false;

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

    public BlurpDatabase autoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
        return this;
    }

    public BlurpDatabase pool(boolean pool) {
        this.pool = pool;
        return this;
    }

    public Database build() {
        if (pool) {
            return buildPooled();
        }
        switch (type) {
            case MYSQL:
                return new MySQLDatabase(host, port, databaseName, user == null ? "root" : user, pass == null ? "" : pass, useSSL, allowPublicKeyRetrieval, autoReconnect);
            case SQLITE:
                return new SQLiteDatabase(filePath);
            case MARIADB:
                return new MariaDBDatabase(host, port, databaseName, user == null ? "root" : user, pass == null ? "" : pass, useSSL, allowPublicKeyRetrieval, autoReconnect);
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

    private Database buildPooled() {
        String url, driver;
        switch (type) {
            case MYSQL:
                driver = "com.mysql.cj.jdbc.Driver";
                url = "jdbc:mysql://" + host + ":" + port + "/" + databaseName
                        + "?useSSL=" + useSSL
                        + "&allowPublicKeyRetrieval=" + allowPublicKeyRetrieval
                        + "&autoReconnect=" + autoReconnect;
                break;
            case MARIADB:
                driver = "org.mariadb.jdbc.Driver";
                url = "jdbc:mariadb://" + host + ":" + port + "/" + databaseName
                        + "?useSSL=" + useSSL
                        + "&allowPublicKeyRetrieval=" + allowPublicKeyRetrieval
                        + "&autoReconnect=" + autoReconnect;
                break;
            case POSTGRESQL:
                driver = "org.postgresql.Driver";
                String sslMode = useSSL ? "require" : "disable";
                url = "jdbc:postgresql://" + host + ":" + port + "/" + databaseName
                        + "?sslmode=" + sslMode;
                break;
            case SQLSERVER:
                driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                url = "jdbc:sqlserver://" + host + ":" + port
                        + ";databaseName=" + databaseName
                        + ";encrypt=" + useSSL
                        + ";trustServerCertificate=true";
                break;
            case ORACLE:
                driver = "oracle.jdbc.OracleDriver";
                url = "jdbc:oracle:thin:@//" + host + ":" + port + "/" + databaseName;
                break;
            case H2:
                driver = "org.h2.Driver";
                url = "jdbc:h2:" + filePath + ";AUTO_SERVER=TRUE";
                break;
            case HSQLDB:
                driver = "org.hsqldb.jdbc.JDBCDriver";
                url = "jdbc:hsqldb:file:" + filePath + ";hsqldb.tx=mvcc;hsqldb.lock_file=false";
                break;
            case SQLITE:
                driver = "org.sqlite.JDBC";
                url = "jdbc:sqlite:" + filePath;
                break;
            default:
                throw new IllegalArgumentException("Cannot pool for DB type: " + type);
        }
        String u = user == null ? defaultUser(type) : user;
        String p = pass == null ? "" : pass;
        return new HikariPooledDatabase(driver, url, u, p);
    }

    private String defaultUser(DatabaseType t) {
        switch (t) {
            case MYSQL: return "root";
            case MARIADB: return "root";
            case POSTGRESQL: return "postgres";
            case SQLSERVER: return "sa";
            case ORACLE: return "system";
            case H2: return "sa";
            case HSQLDB: return "SA";
            default: return "";
        }
    }
}
