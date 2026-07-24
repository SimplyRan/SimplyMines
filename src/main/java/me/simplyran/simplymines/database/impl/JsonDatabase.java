package me.simplyran.simplymines.database.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.database.IDatabase;
import me.simplyran.simplymines.database.MineSerializer;
import me.simplyran.simplymines.objects.BasicMine;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Stores every mine as its own {@code <name>.json} file inside the plugin's
 * {@code mines} folder. This is the default, zero-configuration backend.
 */
public class JsonDatabase implements IDatabase {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final SimplyMines plugin;
    private final MineSerializer serializer;

    public JsonDatabase(@NotNull SimplyMines plugin,
                        @NotNull MineSerializer serializer) {
        this.plugin = plugin;
        this.serializer = serializer;
    }

    @Override
    public List<BasicMine> loadMines() {

        List<BasicMine> mines = new ArrayList<>();

        File minesFolder = new File(plugin.getDataFolder(), "mines");

        if (!minesFolder.exists()) {
            boolean mkdir = minesFolder.mkdirs();
            return mines;
        }

        File[] files = minesFolder.listFiles((dir, fileName) -> fileName.endsWith(".json"));

        if (files == null || files.length == 0) {
            return mines;
        }

        Gson gson = new Gson();

        for (File file : files) {
            String mineName = file.getName().replace(".json", "");

            try (FileReader reader = new FileReader(file)) {

                JsonObject json = gson.fromJson(reader, JsonObject.class);

                BasicMine mine = serializer.deserialize(mineName, json);

                if (mine != null) {
                    mines.add(mine);
                }

            } catch (Exception e) {
                plugin.getLogger().warning(
                        "Failed to load mine '" + mineName + "': " + e.getMessage()
                );
            }
        }

        return mines;
    }

    @Override
    public boolean saveMine(@NotNull BasicMine mine) {

        JsonObject json = serializer.serialize(mine);

        File minesFolder = new File(plugin.getDataFolder(), "mines");

        if (!minesFolder.exists()) {
            boolean mkdir = minesFolder.mkdirs();
        }

        File mineFile = new File(minesFolder, mine.getName() + ".json");

        try (FileWriter writer = new FileWriter(mineFile)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Failed to save mine " + mine.getName(),
                    e
            );
            return false;
        }
        return true;
    }

    @Override
    public void deleteMine(@NotNull String mineName) {

        Path path = Path.of(
                plugin.getDataFolder() + "/mines/" + mineName + ".json"
        );

        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            plugin.getLogger().severe("Error while deleting mine. %s".formatted(e.getMessage()));
        }
    }
}
