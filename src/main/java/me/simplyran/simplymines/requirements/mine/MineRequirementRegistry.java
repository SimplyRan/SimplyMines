package me.simplyran.simplymines.requirements.mine;

import com.google.gson.JsonObject;
import me.simplyran.simplymines.managers.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public final class MineRequirementRegistry {

    private static final Map<String, MineRequirementFactory> REQUIREMENTS = new HashMap<>();

    public static void register(String id, MineRequirementFactory factory) {
        REQUIREMENTS.put(id, factory);
    }

    public static IMineRequirement deserialize(ConfigManager configManager, JsonObject json) {
        String type = json.get("type").getAsString();

        MineRequirementFactory factory = REQUIREMENTS.get(type);

        return factory == null ? null : factory.create(configManager, json);
    }
}