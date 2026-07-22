package me.simplyran.simplymines.actions;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public final class ActionRegistry {

    private static final Map<String, ActionFactory> REQUIREMENTS = new HashMap<>();

    public static void register(String id, ActionFactory factory) {
        REQUIREMENTS.put(id, factory);
    }

    public static IAction deserialize(JsonObject json) {
        String type = json.get("type").getAsString();

        ActionFactory factory = REQUIREMENTS.get(type);

        return factory == null ? null : factory.create(json);
    }
}
