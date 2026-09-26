package me.simplyran.simplymines.requirements.mine.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.utils.AttributeUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The held tool must reach a minimum value for every listed attribute. A key is either a vanilla item attribute
 * or a custom number another plugin stores on the tool, see {@link AttributeUtils}.
 */
public class AttributeMineRequirement implements IMineRequirement {

    public final static String NAME = "attribute_mine_requirement";

    private static final double MAX_VALUE = 1_000_000;

    private static final ConfigData<String> MISSING_ATTRIBUTES = ConfigFactory.newConfigData(
            "messages.missing-attributes", "<#ef6f6c>You need a tool with <attributes> to mine here.");
    private static ConfigManager registeredWith;

    private boolean enabled;
    private final Map<String, Double> attributes = new LinkedHashMap<>();
    private final Map<String, Resolved> resolved = new LinkedHashMap<>();

    /** Lookup keys parsed once, so a block break does no registry or string work. */
    private record Resolved(Attribute vanilla, NamespacedKey custom) {}

    public AttributeMineRequirement(@NotNull ConfigManager configManager) {
        if (registeredWith != configManager) {
            configManager.registerLang(MISSING_ATTRIBUTES);
            registeredWith = configManager;
        }
    }

    public Map<String, Double> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public boolean hasAttribute(@NotNull String key) {
        return attributes.containsKey(key);
    }

    public void setMinimum(@NotNull String key, double minimum) {
        // Rounded so repeated 0.1 steps don't drift (1.2000000000000002) and fail a comparison against the tool.
        attributes.put(key, Math.round(Math.clamp(minimum, -MAX_VALUE, MAX_VALUE) * 10_000) / 10_000.0);

        Attribute vanilla = AttributeUtils.vanilla(key);
        resolved.put(key, new Resolved(vanilla, vanilla == null ? AttributeUtils.parseKey(key) : null));
    }

    public void removeAttribute(@NotNull String key) {
        attributes.remove(key);
        resolved.remove(key);
    }

    @Override
    public boolean isSatisfied(@NotNull Player player) {
        if (!enabled) return true;

        ItemStack tool = player.getInventory().getItemInMainHand();
        for (Map.Entry<String, Double> entry : attributes.entrySet()) {
            Resolved lookup = resolved.get(entry.getKey());
            if (lookup == null) continue;

            double value = AttributeUtils.valueOf(tool, lookup.vanilla(), lookup.custom());
            if (value < entry.getValue()) return false;
        }

        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getSerializationKey() {
        return NAME;
    }

    @Override
    public List<Pair<String, Object>> serialize() {
        return List.of(
                Pair.of("attributes", new LinkedHashMap<>(attributes)),
                Pair.of("enabled", enabled)
        );
    }

    @Override
    public Component denyMessage() {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Double> entry : attributes.entrySet()) {
            names.add(AttributeUtils.describe(entry.getKey(), entry.getValue()));
        }
        return MessageUtils.format(MISSING_ATTRIBUTES,
                "attributes", String.join(", ", names));
    }

    public static IMineRequirement deserialize(ConfigManager configManager, JsonObject json) {
        AttributeMineRequirement requirement = new AttributeMineRequirement(configManager);

        if (json.has("attributes")) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("attributes").entrySet()) {
                requirement.setMinimum(entry.getKey(), entry.getValue().getAsDouble());
            }
        }

        if (json.has("enabled")) {
            requirement.setEnabled(json.get("enabled").getAsBoolean());
        }

        return requirement;
    }
}
