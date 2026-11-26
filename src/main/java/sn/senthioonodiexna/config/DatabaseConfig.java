package sn.senthioonodiexna.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3306;
    private static final String DB_NAME = "sen_thioono_diexna";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static HikariDataSource dataSource;

    private DatabaseConfig() {}

    public static synchronized void initialize() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        String host = System.getProperty("db.host", DEFAULT_HOST);
        int port = Integer.getInteger("db.port", DEFAULT_PORT);
        String user = System.getProperty("db.user", DEFAULT_USER);
        String password = System.getProperty("db.password", DEFAULT_PASS);

        // 1. Assurer la création de la base de données si elle n'existe pas
        ensureDatabaseExists(host, port, user, password);

        // 2. Initialiser le pool de connexion HikariCP
        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8",
                host, port, DB_NAME);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setPoolName("SenThioonoPool");

        try {
            dataSource = new HikariDataSource(config);
            logger.info("Pool de connexions HikariCP initialisé avec succès pour {}", jdbcUrl);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du pool HikariCP avec le mot de passe par défaut. Tentative avec 'root'...", e);
            // Essai de repli avec password 'root'
            config.setPassword("root");
            dataSource = new HikariDataSource(config);
        }

        // 3. Exécuter le schéma et le seed si la table utilisateur n'existe pas encore
        ensureTablesAndSeedData();
    }

    private static void ensureDatabaseExists(String host, String port, String user, String password) {
        // Overload helper
        ensureDatabaseExists(host, Integer.parseInt(port), user, password);
    }

    private static void ensureDatabaseExists(String host, int port, String user, String password) {
        String serverUrl = String.format("jdbc:mysql://%s:%d/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", host, port);
        try (Connection conn = tryConnectServer(serverUrl, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + DB_NAME + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
            logger.info("Base de données {} vérifiée/créée avec succès.", DB_NAME);
        } catch (SQLException e) {
            logger.warn("Attention lors de la création de la base : {}", e.getMessage());
        }
    }

    private static Connection tryConnectServer(String url, String user, String password) throws SQLException {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            if ("".equals(password)) {
                return DriverManager.getConnection(url, user, "root");
            }
            throw e;
        }
    }

    private static void ensureTablesAndSeedData() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            boolean tablesExist = false;
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'utilisateur'")) {
                if (rs.next()) {
                    tablesExist = true;
                }
            }

            if (!tablesExist) {
                logger.info("Initialisation du schéma et des données de base de Sen Thioono Diexna...");
                executeSqlResource(conn, "/db/schema.sql");
                executeSqlResource(conn, "/db/seed_data.sql");
                logger.info("Schéma et données initiales insérés avec succès !");
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification/initialisation des tables : ", e);
        }
    }

    public static void executeSqlResource(Connection conn, String resourcePath) {
        try (InputStream is = DatabaseConfig.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.warn("Ressource SQL introuvable : {}", resourcePath);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Statement stmt = conn.createStatement()) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("--") || line.startsWith("/*") || line.isEmpty()) {
                        continue;
                    }
                    sb.append(line).append(" ");
                    if (line.endsWith(";")) {
                        String sql = sb.toString().trim();
                        if (!sql.isEmpty() && !sql.startsWith("USE") && !sql.startsWith("CREATE DATABASE")) {
                            stmt.execute(sql);
                        }
                        sb = new StringBuilder();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'exécution du script SQL {} : {}", resourcePath, e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initialize();
        }
        return dataSource.getConnection();
    }

    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Pool de connexions fermé.");
        }
    }
}
