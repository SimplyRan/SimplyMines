package me.simplyran.simplymines.database;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.database.impl.JsonDatabase;
import me.simplyran.simplymines.database.impl.MySQLDatabase;
import me.simplyran.simplymines.database.impl.SQLiteDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Builds the configured {@link IDatabase} backend from {@code database.type}.
 * Kept separate from {@code MineManager} so the manager receives its database
 * ready-made (dependency injection) instead of knowing how to construct one.
 */
public final class DatabaseFactory {

    private DatabaseFactory() {
    }

    @NotNull
    public static IDatabase create(@NotNull SimplyMines plugin,
                                   @NotNull MineSerializer serializer) {

        String type = plugin.getConfig().getString("database.type", "JSON")
                .toUpperCase(Locale.ROOT);

        return switch (type) {
            case "MYSQL" -> new MySQLDatabase(plugin, serializer);
            case "SQLITE" -> new SQLiteDatabase(plugin, serializer);
            default -> new JsonDatabase(plugin, serializer);
        };
    }
}
