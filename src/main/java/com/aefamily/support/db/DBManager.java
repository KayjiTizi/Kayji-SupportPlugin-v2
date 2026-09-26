package com.aefamily.support.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
    private final JavaPlugin plugin;
    private final HikariDataSource ds;
    private final boolean mysql;

    public DBManager(JavaPlugin plugin) {
        this.plugin = plugin;
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String type = plugin.getConfig().getString("database.type", "sqlite");
        this.mysql = "mysql".equalsIgnoreCase(type);
        HikariConfig config = new HikariConfig();

        if (mysql) {
            String host = plugin.getConfig().getString("database.mysql.host");
            int port = plugin.getConfig().getInt("database.mysql.port");
            String db = plugin.getConfig().getString("database.mysql.database");
            String user = plugin.getConfig().getString("database.mysql.user");
            String pass = plugin.getConfig().getString("database.mysql.password");
            String jdbc = String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC",
                    host, port, db);
            config.setJdbcUrl(jdbc);
            config.setUsername(user);
            config.setPassword(pass);
        } else {
            String fileName = plugin.getConfig()
                    .getString("database.sqlite.file", "support.db");
            File dbFile = new File(dataFolder, fileName);
            File parent = dbFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            String dbPath = dbFile.getAbsolutePath();
            config.setJdbcUrl("jdbc:sqlite:" + dbPath);
        }

        config.setMaximumPoolSize(6);
        ds = new HikariDataSource(config);
        initTable();
    }

    private void initTable() {
        String tableSql;
        if (mysql) {
            tableSql = "CREATE TABLE IF NOT EXISTS support_requests (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "player VARCHAR(36) NOT NULL," +
                    "message TEXT NOT NULL," +
                    "time VARCHAR(32) NOT NULL," +
                    "status VARCHAR(16) NOT NULL," +
                    "category VARCHAR(32) NOT NULL DEFAULT 'GENERAL'," +
                    "priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL'," +
                    "assigned_staff VARCHAR(36) NULL," +
                    "last_updated VARCHAR(32) NOT NULL DEFAULT ''" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        } else {
            tableSql = "CREATE TABLE IF NOT EXISTS support_requests (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player VARCHAR(36) NOT NULL," +
                    "message TEXT NOT NULL," +
                    "time VARCHAR(32) NOT NULL," +
                    "status VARCHAR(16) NOT NULL," +
                    "category VARCHAR(32) NOT NULL DEFAULT 'GENERAL'," +
                    "priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL'," +
                    "assigned_staff VARCHAR(36)," +
                    "last_updated VARCHAR(32) NOT NULL DEFAULT ''" +
                    ")";
        }
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(tableSql);
            ensureColumns(conn);
        } catch (SQLException e) {
            plugin.getLogger().severe("Không thể tạo/migrate bảng support_requests: " + e.getMessage());
        }
    }

    private void ensureColumns(Connection conn) {
        addColumn(conn, "category VARCHAR(32) NOT NULL DEFAULT 'GENERAL'");
        addColumn(conn, "priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL'");
        addColumn(conn, "assigned_staff VARCHAR(36)");
        addColumn(conn, "last_updated VARCHAR(32) NOT NULL DEFAULT ''");
    }

    private void addColumn(Connection conn, String definition) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE support_requests ADD COLUMN " + definition);
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("duplicate") || msg.contains("Duplicate") || msg.contains("exists") || msg.contains("_ALREADY"))) {
                return;
            }
            // SQLite message for duplicate column
            if (msg != null && msg.toLowerCase().contains("duplicate column name")) {
                return;
            }
            plugin.getLogger().warning("Không thể thêm cột mới (" + definition + "): " + e.getMessage());
        }
    }

    public DataSource getDataSource() {
        return ds;
    }

    public void close() {
        ds.close();
    }
}
