package me.simplyran.simplymines.requirements.reset;

import com.google.gson.JsonObject;
import me.simplyran.simplymines.objects.BasicMine;

import java.util.HashMap;
import java.util.Map;

public final class ResetRequirementRegistry {

    private static final Map<String, ResetRequirementFactory> REQUIREMENTS = new HashMap<>();

    public static void register(String id, ResetRequirementFactory factory) {
        REQUIREMENTS.put(id, factory);
    }

    public static IResetRequirement deserialize(BasicMine mine, JsonObject json) {
        String type = json.get("type").getAsString();

        ResetRequirementFactory factory = REQUIREMENTS.get(type);

        return factory == null ? null : factory.create(mine, json);
    }
}