package me.simplyran.simplymines.utils;

import com.google.gson.*;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.objects.IMine;
import me.simplyran.simplymines.objects.impl.BasicMine;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class JsonUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Defaults used when loading older mine files that predate these fields
    private static final List<Integer> DEFAULT_WARN_SECONDS = List.of(30, 15, 5, 3, 2, 1);
    private static final boolean DEFAULT_WARN_NEAR = true;
    private static final boolean DEFAULT_WARN_GLOBAL = false;
    private static final boolean DEFAULT_TELEPORT_PLAYERS = true;
    private static final int DEFAULT_WARN_DISTANCE = 50;

    public static void loadMines(File dataFolder,
                                 WorkloadRunnable workloadRunnable,
                                 MineManager mineManager) {
        File minesFolder = new File(dataFolder, "mines");
        if (!minesFolder.exists()) {
            minesFolder.mkdirs();
            return;
        }

        File[] files = minesFolder.listFiles((dir, fileName) -> fileName.endsWith(".json"));
        if (files == null || files.length == 0) return;

        Gson gson = new Gson();

        for (File file : files) {
            String mineName = file.getName().substring(0, file.getName().length() - ".json".length());

            try (FileReader reader = new FileReader(file)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);

                int resetTime = json.get("resetTime").getAsInt();
                boolean enabled = json.get("enabled").getAsBoolean();

                String worldName = json.get("world").getAsString();
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    continue;
                }

                JsonObject c1 = json.getAsJsonObject("corner1");

                JsonObject c2 = json.getAsJsonObject("corner2");

                Location corner1 = new Location(world,
                        c1.get("x").getAsDouble(),
                        c1.get("y").getAsDouble(),
                        c1.get("z").getAsDouble());

                Location corner2 = new Location(world,
                        c2.get("x").getAsDouble(),
                        c2.get("y").getAsDouble(),
                        c2.get("z").getAsDouble());

                Map<String, Double> materials = new HashMap<>();
                JsonObject materialsJson = json.getAsJsonObject("materials");
                for (Map.Entry<String, JsonElement> entry : materialsJson.entrySet()) {
                    if (entry.getKey() == null) {
                        continue;
                    }
                    materials.put(entry.getKey(), entry.getValue().getAsDouble());
                }

                List<Integer> warnSeconds = new ArrayList<>(DEFAULT_WARN_SECONDS);
                if (json.has("warnSeconds") && json.get("warnSeconds").isJsonArray()) {
                    warnSeconds = new ArrayList<>();
                    for (JsonElement element : json.getAsJsonArray("warnSeconds")) {
                        warnSeconds.add(element.getAsInt());
                    }
                }

                boolean warnNear = json.has("warnNear")
                        ? json.get("warnNear").getAsBoolean()
                        : DEFAULT_WARN_NEAR;

                boolean warnGlobal = json.has("warnGlobal")
                        ? json.get("warnGlobal").getAsBoolean()
                        : DEFAULT_WARN_GLOBAL;

                boolean teleportPlayers = json.has("teleportPlayers")
                        ? json.get("teleportPlayers").getAsBoolean()
                        : DEFAULT_TELEPORT_PLAYERS;

                int warnDistance = json.has("warnDistance")
                        ? json.get("warnDistance").getAsInt()
                        : DEFAULT_WARN_DISTANCE;

                boolean usePhysics = json.has("usePhysics") && json.get("usePhysics").getAsBoolean();

                BasicMine mine = new BasicMine(enabled,
                        mineName,
                        resetTime,
                        corner1,
                        corner2,
                        materials,
                        workloadRunnable,
                        warnSeconds,
                        warnNear,
                        warnGlobal,
                        teleportPlayers,
                        warnDistance,
                        usePhysics);

                mineManager.addMine(mine);

            } catch (IOException | JsonSyntaxException | NullPointerException e) {
                Bukkit.getLogger().warning("Failed to load mine from '" + file.getName() + "': " + e.getMessage());
            }
        }

        Bukkit.getLogger().info("Loaded " + mineManager.getMines().size() + " mine(s).");
    }


    public static void saveMine(SimplyMines plugin, IMine mine) {
        JsonObject json;

        if (mine instanceof BasicMine basicMine){
            json = serializeBasicMine(basicMine);
        }
        else {
            Bukkit.getLogger().severe("ERROR FOUND");
            return;
        }

        File minesFolder = new File(plugin.getDataFolder(), "mines");
        if (!minesFolder.exists()) {
            minesFolder.mkdirs();
        }

        File mineFile = new File(minesFolder, mine.getName() + ".json");

        try (FileWriter writer = new FileWriter(mineFile)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save mine " + mine.getName(), e);
        }
    }


    private static JsonObject serializeBasicMine(BasicMine mine) {
        JsonObject json = new JsonObject();
        json.addProperty("enabled", mine.isEnabled());
        json.addProperty("resetTime", mine.getResetTime());

        BoxedRegion region = mine.getRegion();

        json.addProperty("world", region.getWorld().getName());

        JsonObject c1 = new JsonObject();
        c1.addProperty("x", region.getMaxX());
        c1.addProperty("y", region.getMaxY());
        c1.addProperty("z", region.getMaxZ());
        json.add("corner1", c1);

        JsonObject c2 = new JsonObject();
        c2.addProperty("x", region.getMinX());
        c2.addProperty("y", region.getMinY());
        c2.addProperty("z", region.getMinZ());
        json.add("corner2", c2);

        JsonObject materials = new JsonObject();
        for (Map.Entry<String, Double> entry : mine.getMaterials()) {
            materials.addProperty(entry.getKey(), entry.getValue());
        }
        json.add("materials", materials);

        JsonArray warnSeconds = new JsonArray();
        for (Integer second : mine.getWarnSeconds()) {
            warnSeconds.add(second);
        }
        json.add("warnSeconds", warnSeconds);

        json.addProperty("warnNear", mine.isWarnNear());
        json.addProperty("warnGlobal", mine.isWarnGlobal());
        json.addProperty("teleportPlayers", mine.isTeleportPlayers());
        json.addProperty("warnDistance", mine.getWarnDistance());
        json.addProperty("usePhysics", mine.isUsePhysics());

        return json;
    }
}