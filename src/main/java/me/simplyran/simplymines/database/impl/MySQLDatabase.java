package me.simplyran.simplymines.database.impl;

import com.zaxxer.hikari.HikariConfig;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.database.MineSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Server-backed SQL backend for sharing mines across multiple servers.
 * Connection details are read from the {@code database.mysql} section of
 * {@code config.yml}.
 */
public class MySQLDatabase extends SqlDatabase {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                    + "name VARCHAR(255) PRIMARY KEY, "
                    + "data LONGTEXT NOT NULL"
                    + ") DEFAULT CHARSET=utf8mb4";

    private static final String UPSERT_SQL =
            "INSERT INTO " + TABLE + " (name, data) VALUES (?, ?) "
                    + "ON DUPLICATE KEY UPDATE data = VALUES(data)";

    public MySQLDatabase(@NotNull SimplyMines plugin,
                         @NotNull MineSerializer serializer) {
        super(plugin, serializer, buildConfig(plugin), CREATE_TABLE_SQL, UPSERT_SQL);
    }

    private static HikariConfig buildConfig(SimplyMines plugin) {
        ConfigurationSection mysql = plugin.getConfig().getConfigurationSection("database.mysql");

        String host = mysql != null ? mysql.getString("host", "localhost") : "localhost";
        int port = mysql != null ? mysql.getInt("port", 3306) : 3306;
        String database = mysql != null ? mysql.getString("database", "simplymines") : "simplymines";
        String username = mysql != null ? mysql.getString("username", "root") : "root";
        String password = mysql != null ? mysql.getString("password", "") : "";
        boolean useSsl = mysql != null && mysql.getBoolean("useSSL", false);
        int poolSize = mysql != null ? mysql.getInt("pool-size", 10) : 10;

        if (!host.matches("[A-Za-z0-9.\\[\\]:_-]+")) {
            plugin.getLogger().severe("Invalid MySQL host '" + host + "', falling back to localhost.");
            host = "localhost";
        }
        if (!database.matches("[A-Za-z0-9_-]+")) {
            plugin.getLogger().severe("Invalid MySQL database name '" + database + "', falling back to simplymines.");
            database = "simplymines";
        }

        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("SimplyMines-MySQL");
        hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + useSsl + "&useUnicode=true&characterEncoding=utf8");
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(Math.max(1, poolSize));
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ConfigurationSection properties = mysql != null ? mysql.getConfigurationSection("properties") : null;
        if (properties != null) {
            for (String key : properties.getKeys(false)) {
                hikari.addDataSourceProperty(key, String.valueOf(properties.get(key)));
            }
        }
        return hikari;
    }
}
