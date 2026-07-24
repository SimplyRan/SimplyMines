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
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
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

    private static final List<Integer> DEFAULT_WARN_SECONDS = List.of(30, 15, 5, 3, 2, 1);
    private static final boolean DEFAULT_WARN_NEAR = true;
    private static final boolean DEFAULT_WARN_GLOBAL = false;
    private static final boolean DEFAULT_TELEPORT_PLAYERS = true;
    private static final int DEFAULT_WARN_DISTANCE = 50;

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

        boolean enabled = json.get("enabled").getAsBoolean();

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

        boolean normalDropsEnabled = json.has("normalDropsEnabled")
                && json.get("normalDropsEnabled").getAsBoolean();

        boolean autoPickup = json.has("autoPickup")
                && json.get("autoPickup").getAsBoolean();


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
                replaceMode,
                normalDropsEnabled,
                autoPickup
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
        if (json.has("block_actions")) {
            JsonObject blockActionsJson = json.getAsJsonObject("block_actions");
            for (Map.Entry<String, JsonElement> entry : blockActionsJson.entrySet()) {
                String blockKey = entry.getKey();
                JsonArray actionsArray = entry.getValue().getAsJsonArray();

                for (JsonElement element : actionsArray) {
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
        json.addProperty("normalDropsEnabled", mine.isNormalDropsEnabled());
        json.addProperty("autoPickup", mine.isAutoPickup());

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
