package me.simplyran.simplymines.actions;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public final class ActionRegistry {

    private static final Map<String, ActionFactory> ACTIONS = new HashMap<>();

    public static void register(String id, ActionFactory factory) {
        ACTIONS.put(id, factory);
    }

    public static IAction deserialize(JsonObject json) {
        String type = json.get("type").getAsString();

        ActionFactory factory = ACTIONS.get(type);

        return factory == null ? null : factory.create(json);
    }
}
