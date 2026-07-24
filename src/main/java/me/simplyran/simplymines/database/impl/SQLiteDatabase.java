package me.simplyran.simplymines.database.impl;

import com.zaxxer.hikari.HikariConfig;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.database.MineSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * File-backed SQL backend. Stores every mine in a single {@code mines.db}
 * SQLite file inside the plugin folder. Requires no external server, so it is
 * the recommended option for a single-server setup that wants a database.
 */
public class SQLiteDatabase extends SqlDatabase {

    public SQLiteDatabase(@NotNull SimplyMines plugin,
                          @NotNull MineSerializer serializer) {
        super(plugin, serializer);
    }

    @Override
    protected HikariConfig buildHikariConfig() {
        String fileName = plugin.getConfig().getString("database.sqlite.file", "mines.db");
        File databaseFile = new File(plugin.getDataFolder(), fileName);

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        HikariConfig config = new HikariConfig();
        config.setPoolName("SimplyMines-SQLite");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        // SQLite allows only one writer at a time; a single connection avoids
        // "database is locked" errors from concurrent pool connections.
        config.setMaximumPoolSize(1);
        return config;
    }

    @Override
    protected String createTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                + "name VARCHAR(255) PRIMARY KEY, "
                + "data TEXT NOT NULL"
                + ")";
    }

    @Override
    protected String upsertSql() {
        return "INSERT INTO " + TABLE + " (name, data) VALUES (?, ?) "
                + "ON CONFLICT(name) DO UPDATE SET data = excluded.data";
    }
}
