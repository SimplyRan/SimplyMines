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

    public MySQLDatabase(@NotNull SimplyMines plugin,
                         @NotNull MineSerializer serializer) {
        super(plugin, serializer);
    }

    @Override
    protected HikariConfig buildHikariConfig() {
        ConfigurationSection mysql = plugin.getConfig().getConfigurationSection("database.mysql");
        if (mysql == null) {
            mysql = plugin.getConfig().createSection("database.mysql");
        }

        String host = mysql.getString("host", "localhost");
        int port = mysql.getInt("port", 3306);
        String database = mysql.getString("database", "simplymines");
        String username = mysql.getString("username", "root");
        String password = mysql.getString("password", "");
        boolean useSsl = mysql.getBoolean("useSSL", false);
        int poolSize = mysql.getInt("pool-size", 10);

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

        // Optional advanced JDBC properties from config.
        ConfigurationSection properties = mysql.getConfigurationSection("properties");
        if (properties != null) {
            for (String key : properties.getKeys(false)) {
                hikari.addDataSourceProperty(key, String.valueOf(properties.get(key)));
            }
        }
        return hikari;
    }

    @Override
    protected String createTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                + "name VARCHAR(255) PRIMARY KEY, "
                + "data LONGTEXT NOT NULL"
                + ") DEFAULT CHARSET=utf8mb4";
    }

    @Override
    protected String upsertSql() {
        return "INSERT INTO " + TABLE + " (name, data) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE data = VALUES(data)";
    }
}
