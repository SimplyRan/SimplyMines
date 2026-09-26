package me.simplyran.simplymines.requirements.mine.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.utils.EnchantmentUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentMineRequirement implements IMineRequirement {

    public final static String NAME = "enchantment_mine_requirement";

    private static final int MAX_LEVEL = 255;

    private static final ConfigData<String> MISSING_ENCHANTMENTS = ConfigFactory.newConfigData(
            "messages.missing-enchantments", "<#ef6f6c>You need a tool with <enchantments> to mine here.");
    private static ConfigManager registeredWith;

    private boolean enabled;
    private final Map<String, Integer> enchantments = new LinkedHashMap<>();
    private final Map<String, Enchantment> resolved = new HashMap<>();

    public EnchantmentMineRequirement(@NotNull ConfigManager configManager) {
        if (registeredWith != configManager) {
            configManager.registerLang(MISSING_ENCHANTMENTS);
            registeredWith = configManager;
        }
    }

    public Map<String, Integer> getEnchantments() {
        return Collections.unmodifiableMap(enchantments);
    }

    public boolean hasEnchantment(@NotNull String key) {
        return enchantments.containsKey(key);
    }

    public void setLevel(@NotNull String key, int level) {
        enchantments.put(key, Math.max(1, Math.min(level, MAX_LEVEL)));

        Enchantment enchantment = EnchantmentUtils.fromKey(key);
        if (enchantment != null) {
            resolved.put(key, enchantment);
        } else {
            resolved.remove(key);
        }
    }

    public void removeEnchantment(@NotNull String key) {
        enchantments.remove(key);
        resolved.remove(key);
    }

    @Override
    public boolean isSatisfied(@NotNull Player player) {
        if (!enabled) return true;

        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = resolved.get(entry.getKey());
            if (enchantment == null) continue;

            int level = player.getInventory().getItemInMainHand().getEnchantmentLevel(enchantment);
            if (level < entry.getValue()) return false;
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
                Pair.of("enchantments", new LinkedHashMap<>(enchantments)),
                Pair.of("enabled", enabled)
        );
    }

    @Override
    public Component denyMessage() {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            names.add(EnchantmentUtils.displayName(entry.getKey(), entry.getValue()));
        }
        return MessageUtils.format(MISSING_ENCHANTMENTS,
                "enchantments", String.join(", ", names));
    }

    public static IMineRequirement deserialize(ConfigManager configManager, JsonObject json) {
        EnchantmentMineRequirement requirement = new EnchantmentMineRequirement(configManager);

        if (json.has("enchantments")) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("enchantments").entrySet()) {
                requirement.setLevel(entry.getKey(), entry.getValue().getAsInt());
            }
        }

        if (json.has("enabled")) {
            requirement.setEnabled(json.get("enabled").getAsBoolean());
        }

        return requirement;
    }
}
