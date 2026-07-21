package me.simplyran.simplymines.utils;

import com.google.gson.*;
import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.requirements.mine.MineRequirementRegistry;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import me.simplyran.simplymines.requirements.reset.ResetRequirementRegistry;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

public class JsonUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final List<Integer> DEFAULT_WARN_SECONDS = List.of(30, 15, 5, 3, 2, 1);
    private static final boolean DEFAULT_WARN_NEAR = true;
    private static final boolean DEFAULT_WARN_GLOBAL = false;
    private static final boolean DEFAULT_TELEPORT_PLAYERS = true;
    private static final int DEFAULT_WARN_DISTANCE = 50;

    public static void loadMines(File dataFolder,
                                 WorkloadRunnable workloadRunnable,
                                 MineManager mineManager,
                                 ConfigManager configManager) {

        File minesFolder = new File(dataFolder, "mines");

        if (!minesFolder.exists()) {
            boolean mkdirs = minesFolder.mkdirs();
            return;
        }

        File[] files = minesFolder.listFiles((dir, fileName) -> fileName.endsWith(".json"));

        if (files == null || files.length == 0) {
            return;
        }

        Gson gson = new Gson();

        for (File file : files) {
            String mineName = file.getName().replace(".json", "");

            try (FileReader reader = new FileReader(file)) {

                JsonObject json = gson.fromJson(reader, JsonObject.class);

                boolean enabled = json.get("enabled").getAsBoolean();

                String worldName = json.get("world").getAsString();
                World world = Bukkit.getWorld(worldName);

                if (world == null) {
                    Bukkit.getLogger().warning("Could not find world '" + worldName + "' for mine '" + mineName + "'");
                    continue;
                }

                JsonObject c1 = json.getAsJsonObject("corner1");
                JsonObject c2 = json.getAsJsonObject("corner2");

                Location corner1 = new Location(
                        world,
                        c1.get("x").getAsDouble(),
                        c1.get("y").getAsDouble(),
                        c1.get("z").getAsDouble()
                );

                Location corner2 = new Location(
                        world,
                        c2.get("x").getAsDouble(),
                        c2.get("y").getAsDouble(),
                        c2.get("z").getAsDouble()
                );

                Map<String, Double> materials = new HashMap<>();

                JsonObject materialsJson = json.getAsJsonObject("materials");

                if (materialsJson != null) {
                    for (Map.Entry<String, JsonElement> entry : materialsJson.entrySet()) {
                        materials.put(entry.getKey(), entry.getValue().getAsDouble());
                    }
                }

                List<Integer> warnSeconds = new ArrayList<>(DEFAULT_WARN_SECONDS);

                if (json.has("warnSeconds")) {
                    warnSeconds.clear();

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

                boolean usePhysics = json.has("usePhysics")
                        && json.get("usePhysics").getAsBoolean();

                boolean replaceMode = json.has("replaceMode")
                        && json.get("replaceMode").getAsBoolean();

                BasicMine mine = new BasicMine(
                        enabled,
                        mineName,
                        corner1,
                        corner2,
                        materials,
                        workloadRunnable,
                        warnSeconds,
                        warnNear,
                        warnGlobal,
                        teleportPlayers,
                        warnDistance,
                        usePhysics,
                        replaceMode
                );

                if (json.has("teleportLocation")) {
                    JsonObject tp = json.getAsJsonObject("teleportLocation");

                    mine.setTeleportLocation(
                            new Location(
                                    world,
                                    tp.get("x").getAsDouble(),
                                    tp.get("y").getAsDouble(),
                                    tp.get("z").getAsDouble(),
                                    tp.has("yaw") ? tp.get("yaw").getAsFloat() : 0f,
                                    tp.has("pitch") ? tp.get("pitch").getAsFloat() : 0f
                            )
                    );
                }

                if (json.has("mine_requirements")) {
                    JsonArray requirements = json.getAsJsonArray("mine_requirements");

                    for (JsonElement element : requirements) {
                        IMineRequirement requirement =
                                MineRequirementRegistry.deserialize(
                                        configManager,
                                        element.getAsJsonObject()
                                );

                        if (requirement != null) {
                            mine.addMineRequirement(requirement);
                        }
                    }
                }

                if (json.has("reset_requirements")) {
                    JsonArray requirements = json.getAsJsonArray("reset_requirements");

                    for (JsonElement element : requirements) {
                        IResetRequirement requirement =
                                ResetRequirementRegistry.deserialize(
                                        mine,
                                        element.getAsJsonObject()
                                );

                        if (requirement != null) {
                            mine.addResetRequirement(requirement);
                        }
                    }
                }

                mineManager.addMine(mine);

            } catch (Exception e) {
                Bukkit.getLogger().warning(
                        "Failed to load mine '" + mineName + "': " + e.getMessage()
                );
            }
        }

        Bukkit.getLogger().info(
                "Loaded " + mineManager.getMines().size() + " mine(s)."
        );
    }

    public static boolean saveMine(SimplyMines plugin, BasicMine mine) {

        JsonObject json = serializeBasicMine(mine);

        File minesFolder = new File(plugin.getDataFolder(), "mines");

        if (!minesFolder.exists()) {
            minesFolder.mkdirs();
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

    private static JsonObject serializeBasicMine(BasicMine mine) {

        JsonObject json = new JsonObject();

        json.addProperty("enabled", mine.isEnabled());

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
        json.addProperty("replaceMode", mine.isReplaceMode());

        JsonArray mineRequirements = new JsonArray();

        for (IMineRequirement requirement : mine.getMineRequirements()) {

            JsonObject requirementJson = new JsonObject();

            requirementJson.addProperty(
                    "type",
                    getRequirementName(requirement)
            );

            for (Pair<String, Object> pair : requirement.serialize()) {
                addProperty(
                        requirementJson,
                        pair.first(),
                        pair.right()
                );
            }

            mineRequirements.add(requirementJson);
        }

        json.add("mine_requirements", mineRequirements);

        JsonArray resetRequirements = new JsonArray();

        for (IResetRequirement requirement : mine.getResetRequirements()) {

            JsonObject requirementJson = new JsonObject();

            requirementJson.addProperty(
                    "type",
                    getRequirementName(requirement)
            );

            for (Pair<String, Object> pair : requirement.serialize()) {
                addProperty(
                        requirementJson,
                        pair.first(),
                        pair.right()
                );
            }

            resetRequirements.add(requirementJson);
        }

        json.add("reset_requirements", resetRequirements);

        Location teleportLocation = mine.getTeleportLocation();

        if (teleportLocation != null) {

            JsonObject teleport = new JsonObject();

            teleport.addProperty("x", teleportLocation.getX());
            teleport.addProperty("y", teleportLocation.getY());
            teleport.addProperty("z", teleportLocation.getZ());
            teleport.addProperty("yaw", teleportLocation.getYaw());
            teleport.addProperty("pitch", teleportLocation.getPitch());

            json.add("teleportLocation", teleport);
        }

        return json;
    }

    private static String getRequirementName(Object requirement) {

        try {
            Field field = requirement.getClass().getField("NAME");
            return (String) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Requirement " + requirement.getClass().getName()
                            + " is missing public static final String NAME",
                    e
            );
        }
    }

    public static void deleteMine(SimplyMines plugin, String mineName) {

        Path path = Path.of(
                plugin.getDataFolder() + "/mines/" + mineName + ".json"
        );

        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            plugin.getLogger().severe("Error while deleting mine. %s".formatted(e.getMessage()));
        }
    }

    private static void addProperty(JsonObject json,
                                    String key,
                                    Object value) {

        if (value instanceof String s) {
            json.addProperty(key, s);
        } else if (value instanceof Integer i) {
            json.addProperty(key, i);
        } else if (value instanceof Long l) {
            json.addProperty(key, l);
        } else if (value instanceof Double d) {
            json.addProperty(key, d);
        } else if (value instanceof Float f) {
            json.addProperty(key, f);
        } else if (value instanceof Boolean b) {
            json.addProperty(key, b);
        } else if (value instanceof Character c) {
            json.addProperty(key, c);
        }
    }
}