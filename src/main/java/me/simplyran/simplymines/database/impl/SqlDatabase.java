package me.simplyran.simplymines.database.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.database.IDatabase;
import me.simplyran.simplymines.database.MineSerializer;
import me.simplyran.simplymines.objects.BasicMine;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Shared JDBC backend. Each mine is stored as a single row keyed by its name,
 * with the full mine serialized to a JSON string in the {@code data} column.
 */
public abstract class SqlDatabase implements IDatabase {

    protected static final String TABLE = "simplymines_mines";

    private static final Gson GSON = new Gson();

    protected final SimplyMines plugin;
    private final MineSerializer serializer;
    private final String upsertSql;
    private final HikariDataSource dataSource;

    protected SqlDatabase(@NotNull SimplyMines plugin,
                          @NotNull MineSerializer serializer,
                          @NotNull HikariConfig config,
                          @NotNull String createTableSql,
                          @NotNull String upsertSql) {
        this.plugin = plugin;
        this.serializer = serializer;
        this.upsertSql = upsertSql;

        config.setConnectionTimeout(5_000);
        config.setInitializationFailTimeout(10_000);

        this.dataSource = new HikariDataSource(config);
        createTable(createTableSql);
    }

    private void createTable(String createTableSql) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create mines table", e);
        }
    }

    @Override
    public List<BasicMine> loadMines() {

        List<BasicMine> mines = new ArrayList<>();

        String sql = "SELECT name, data FROM " + TABLE;

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {

            while (result.next()) {
                String mineName = result.getString("name");
                String data = result.getString("data");

                try {
                    JsonObject json = GSON.fromJson(data, JsonObject.class);
                    BasicMine mine = serializer.deserialize(mineName, json);

                    if (mine != null) {
                        mines.add(mine);
                    } else {
                        plugin.getLogger().warning(
                                "Mine '" + mineName + "' was skipped and its row remains in the database.");
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning(
                            "Failed to load mine '" + mineName + "': " + e.getMessage()
                                    + " (its row remains in the database)"
                    );
                }
            }

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load mines from database", e);
        }

        return mines;
    }

    @Override
    public boolean saveMine(@NotNull String mineName, @NotNull JsonObject data) {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(upsertSql)) {

            statement.setString(1, mineName);
            statement.setString(2, GSON.toJson(data));
            statement.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save mine " + mineName, e);
            return false;
        }
        return true;
    }

    @Override
    public void deleteMine(@NotNull String mineName) {

        String sql = "DELETE FROM " + TABLE + " WHERE name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, mineName);
            statement.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete mine " + mineName, e);
        }
    }

    @Override
    public void close() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
