package me.simplyran.simplymines.database.impl;

import com.zaxxer.hikari.HikariConfig;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.database.MineSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * File-backed SQL backend. Stores every mine in a single SQLite file inside
 * the plugin folder. Requires no external server.
 */
public class SQLiteDatabase extends SqlDatabase {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                    + "name VARCHAR(255) PRIMARY KEY, "
                    + "data TEXT NOT NULL"
                    + ")";

    private static final String UPSERT_SQL =
            "INSERT INTO " + TABLE + " (name, data) VALUES (?, ?) "
                    + "ON CONFLICT(name) DO UPDATE SET data = excluded.data";

    public SQLiteDatabase(@NotNull SimplyMines plugin,
                          @NotNull MineSerializer serializer) {
        super(plugin, serializer, buildConfig(plugin), CREATE_TABLE_SQL, UPSERT_SQL);
    }

    private static HikariConfig buildConfig(SimplyMines plugin) {
        String fileName = plugin.getConfig().getString("database.sqlite.file", "mines.db");
        File databaseFile = new File(plugin.getDataFolder(), fileName);

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        HikariConfig config = new HikariConfig();
        config.setPoolName("SimplyMines-SQLite");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        // SQLite allows only one writer at a time.
        config.setMaximumPoolSize(1);
        return config;
    }
}
