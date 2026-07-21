package me.simplyran.simplymines.requirements.mine.impl;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EfficiencyMineRequirement implements IMineRequirement {

    public final static String NAME = "efficiency_mine_requirement";

    private static final ConfigData<String> HIGHER_EFFICIENCY_LEVEL = ConfigFactory.newConfigData(
            "messages.higher-efficiency-level", "<red>You need a tool with Efficiency <level> or higher to mine here.");
    private static boolean registered = false;

    private boolean enabled;
    @Getter @Setter int efficiencyLevel;

    public EfficiencyMineRequirement(@NotNull ConfigManager configManager, int efficiencyLevel) {
        this.efficiencyLevel = efficiencyLevel;
        if (!registered) {
            configManager.register(HIGHER_EFFICIENCY_LEVEL);
            registered = true;
        }
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
        return List.of(
                Pair.of("efficiency_level", efficiencyLevel),
                Pair.of("enabled", enabled)
        );
    }

    @Override
    public Component denyMessage() {
        return MessageUtils.format(HIGHER_EFFICIENCY_LEVEL,
                "level", String.valueOf(efficiencyLevel));
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