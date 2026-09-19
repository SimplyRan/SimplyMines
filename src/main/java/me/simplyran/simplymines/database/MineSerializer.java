package me.simplyran.simplymines.database;

import com.google.gson.*;
import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.actions.ActionRegistry;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.requirements.mine.MineRequirementRegistry;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import me.simplyran.simplymines.requirements.reset.ResetRequirementRegistry;
import me.simplyran.simplymines.settings.MineSettings;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Converts a {@link BasicMine} to and from the registry-based JSON format.
 * Shared by every {@link IDatabase} implementation so the on-disk shape stays
 * identical whether a mine is stored as a file or inside a SQL column.
 */
public class MineSerializer {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final WorkloadRunnable workloadRunnable;
    private final ConfigManager configManager;
    private final Logger logger;

    public MineSerializer(@NotNull WorkloadRunnable workloadRunnable,
                          @NotNull ConfigManager configManager,
                          @NotNull Logger logger) {
        this.workloadRunnable = workloadRunnable;
        this.configManager = configManager;
        this.logger = logger;
    }

    /**
     * Rebuilds a mine from its JSON representation.
     *
     * @return the mine, or {@code null} if its world is not loaded.
     */
    @Nullable
    public BasicMine deserialize(@NotNull String mineName, @NotNull JsonObject json) {

        String worldName = json.get("world").getAsString();
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            logger.warning("Could not find world '" + worldName + "' for mine '" + mineName + "'");
            return null;
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

        // Build settings, falling back to MineSettings defaults for old saves.
        MineSettings settings = new MineSettings(
                getBool(json, "enabled",            MineSettings.DEFAULT_ENABLED),
                getBool(json, "warnNear",           MineSettings.DEFAULT_WARN_NEAR),
                getBool(json, "warnGlobal",         MineSettings.DEFAULT_WARN_GLOBAL),
                readWarnSeconds(json),
                getBool(json, "teleportPlayers",    MineSettings.DEFAULT_TELEPORT_PLAYERS),
                getInt (json, "warnDistance",       MineSettings.DEFAULT_WARN_DISTANCE),
                getBool(json, "usePhysics",         MineSettings.DEFAULT_USE_PHYSICS),
                getBool(json, "replaceMode",        MineSettings.DEFAULT_REPLACE_MODE),
                getBool(json, "normalDropsEnabled", MineSettings.DEFAULT_NORMAL_DROPS_ENABLED),
                getBool(json, "fortuneEnabled",     MineSettings.DEFAULT_FORTUNE_ENABLED),
                getBool(json, "autoPickup",         MineSettings.DEFAULT_AUTO_PICKUP),
                getBool(json, "autoSmelt",          MineSettings.DEFAULT_AUTO_SMELT),
                readTeleportLocation(json, world)
        );

        BasicMine mine = new BasicMine(
                mineName,
                corner1,
                corner2,
                materials,
                workloadRunnable,
                settings
        );

        if (json.has("mine_requirements")) {
            for (JsonElement element : json.getAsJsonArray("mine_requirements")) {
                IMineRequirement requirement =
                        MineRequirementRegistry.deserialize(configManager, element.getAsJsonObject());
                if (requirement != null) {
                    mine.addMineRequirement(requirement);
                }
            }
        }

        if (json.has("reset_requirements")) {
            for (JsonElement element : json.getAsJsonArray("reset_requirements")) {
                IResetRequirement requirement =
                        ResetRequirementRegistry.deserialize(mine, element.getAsJsonObject());
                if (requirement != null) {
                    mine.addResetRequirement(requirement);
                }
            }
        }

        if (json.has("block_actions")) {
            JsonObject blockActionsJson = json.getAsJsonObject("block_actions");
            for (Map.Entry<String, JsonElement> entry : blockActionsJson.entrySet()) {
                String blockKey = entry.getKey();
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    IAction action = ActionRegistry.deserialize(element.getAsJsonObject());
                    if (action != null) {
                        mine.addAction(blockKey, action);
                    }
                }
            }
        }

        return mine;
    }

    @NotNull
    public JsonObject serialize(@NotNull BasicMine mine) {

        JsonObject json = new JsonObject();

        BoxedRegion region = mine.getRegion();
        MineSettings s = mine.getSettings();

        json.addProperty("enabled",           s.isEnabled());
        json.addProperty("world",             region.getWorld().getName());

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
        for (Integer second : s.getWarnSeconds()) {
            warnSeconds.add(second);
        }
        json.add("warnSeconds", warnSeconds);

        json.addProperty("warnNear",           s.isWarnNear());
        json.addProperty("warnGlobal",         s.isWarnGlobal());
        json.addProperty("teleportPlayers",    s.isTeleportPlayers());
        json.addProperty("warnDistance",       s.getWarnDistance());
        json.addProperty("usePhysics",         s.isUsePhysics());
        json.addProperty("replaceMode",        s.isReplaceMode());
        json.addProperty("normalDropsEnabled", s.isNormalDropsEnabled());
        json.addProperty("fortuneEnabled",     s.isFortuneEnabled());
        json.addProperty("autoPickup",         s.isAutoPickup());
        json.addProperty("autoSmelt",          s.isAutoSmelt());

        // Mine requirements — use getSerializationKey() instead of reflection
        JsonArray mineRequirements = new JsonArray();
        for (IMineRequirement requirement : mine.getMineRequirements()) {
            JsonObject requirementJson = new JsonObject();
            requirementJson.addProperty("type", requirement.getSerializationKey());
            for (Pair<String, Object> pair : requirement.serialize()) {
                if (pair.right() instanceof Map || pair.right() instanceof List) {
                    requirementJson.add(pair.first(), GSON.toJsonTree(pair.right()));
                } else {
                    addProperty(requirementJson, pair.first(), pair.right());
                }
            }
            mineRequirements.add(requirementJson);
        }
        json.add("mine_requirements", mineRequirements);

        // Reset requirements
        JsonArray resetRequirements = new JsonArray();
        for (IResetRequirement requirement : mine.getResetRequirements()) {
            JsonObject requirementJson = new JsonObject();
            requirementJson.addProperty("type", requirement.getSerializationKey());
            for (Pair<String, Object> pair : requirement.serialize()) {
                addProperty(requirementJson, pair.first(), pair.right());
            }
            resetRequirements.add(requirementJson);
        }
        json.add("reset_requirements", resetRequirements);

        // Block actions
        JsonObject blockActionsJson = new JsonObject();
        for (Map.Entry<String, List<IAction>> entry : mine.getAllActions().entrySet()) {
            JsonArray actionList = new JsonArray();
            for (IAction action : entry.getValue()) {
                JsonObject actionJson = new JsonObject();
                actionJson.addProperty("type", action.name());
                for (Pair<String, Object> pair : action.serialize()) {
                    if (pair.right() instanceof Map || pair.right() instanceof List) {
                        actionJson.add(pair.first(), GSON.toJsonTree(pair.right()));
                    } else {
                        addProperty(actionJson, pair.first(), pair.right());
                    }
                }
                actionList.add(actionJson);
            }
            blockActionsJson.add(entry.getKey(), actionList);
        }
        json.add("block_actions", blockActionsJson);

        // Teleport location
        Location teleportLocation = s.getTeleportLocation();
        if (teleportLocation != null) {
            JsonObject teleport = new JsonObject();
            teleport.addProperty("x",     teleportLocation.getX());
            teleport.addProperty("y",     teleportLocation.getY());
            teleport.addProperty("z",     teleportLocation.getZ());
            teleport.addProperty("yaw",   teleportLocation.getYaw());
            teleport.addProperty("pitch", teleportLocation.getPitch());
            json.add("teleportLocation", teleport);
        }

        return json;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private static boolean getBool(JsonObject json, String key, boolean fallback) {
        return json.has(key) ? json.get(key).getAsBoolean() : fallback;
    }

    private static int getInt(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    private static List<Integer> readWarnSeconds(JsonObject json) {
        if (!json.has("warnSeconds")) {
            return new ArrayList<>(MineSettings.DEFAULT_WARN_SECONDS);
        }
        List<Integer> list = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("warnSeconds")) {
            list.add(element.getAsInt());
        }
        return list;
    }

    @Nullable
    private static Location readTeleportLocation(JsonObject json, World world) {
        if (!json.has("teleportLocation")) return null;
        JsonObject tp = json.getAsJsonObject("teleportLocation");
        return new Location(
                world,
                tp.get("x").getAsDouble(),
                tp.get("y").getAsDouble(),
                tp.get("z").getAsDouble(),
                tp.has("yaw")   ? tp.get("yaw").getAsFloat()   : 0f,
                tp.has("pitch") ? tp.get("pitch").getAsFloat() : 0f
        );
    }

    private static void addProperty(JsonObject json, String key, Object value) {
        if (value instanceof String s)         json.addProperty(key, s);
        else if (value instanceof Integer i)   json.addProperty(key, i);
        else if (value instanceof Long l)      json.addProperty(key, l);
        else if (value instanceof Double d)    json.addProperty(key, d);
        else if (value instanceof Float f)     json.addProperty(key, f);
        else if (value instanceof Boolean b)   json.addProperty(key, b);
        else if (value instanceof Character c) json.addProperty(key, c);
    }
}
