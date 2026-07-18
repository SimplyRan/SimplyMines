package me.simplyran.simplymines.requirements.mine.impl;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EfficiencyMineRequirement implements IMineRequirement {

    public final static String NAME = "efficiency_mine_requirement";

    private final ConfigManager configManager;
    private boolean enabled;
    @Getter @Setter int efficiencyLevel;

    public EfficiencyMineRequirement(@NotNull ConfigManager configManager, int efficiencyLevel) {
        this.configManager = configManager;
        this.efficiencyLevel = efficiencyLevel;
    }

    @Override
    public boolean isSatisfied(@NotNull Player player) {
        if (!enabled) return true;
        return player.getInventory()
                .getItemInMainHand()
                .getEnchantmentLevel(Enchantment.EFFICIENCY) >= efficiencyLevel;
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
    public List<Pair<String, Object>> serialize() {
        return List.of(Pair.of("efficiency_level", efficiencyLevel));
    }

    @Override
    public Component denyMessage() {
        return configManager.getMessage("higher-efficiency-level", "level", String.valueOf(efficiencyLevel));
    }

    public static IMineRequirement deserialize(ConfigManager configManager, JsonObject json) {
        EfficiencyMineRequirement requirement =
                new EfficiencyMineRequirement(configManager, json.get("efficiency_level").getAsInt());

        if (json.has("enabled")) {
            requirement.setEnabled(json.get("enabled").getAsBoolean());
        }

        return requirement;
    }
}