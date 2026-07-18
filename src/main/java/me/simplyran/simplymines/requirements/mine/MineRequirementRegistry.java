package me.simplyran.simplymines.requirements.mine;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class MineRequirementRegistry {

    private static final Map<String, Function<JsonObject, IMineRequirement>> REQUIREMENTS = new HashMap<>();

    public static void register(String id, Function<JsonObject, IMineRequirement> factory) {
        REQUIREMENTS.put(id, factory);
    }

    public static IMineRequirement deserialize(JsonObject json) {
        String type = json.get("type").getAsString();

        Function<JsonObject, IMineRequirement> factory = REQUIREMENTS.get(type);

        return factory == null ? null : factory.apply(json);
    }
}