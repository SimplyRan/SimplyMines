package me.simplyran.simplymines.requirements.mine.impl;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PermissionMineRequirement implements IMineRequirement {

    public final static String NAME = "permission_mine_requirement";

    private final ConfigManager configManager;
    private boolean enabled;
    @Getter @Setter private String permission;

    public PermissionMineRequirement(@NotNull ConfigManager configManager, @NotNull String permission) {
        this.configManager = configManager;
        this.permission = permission;
    }

    @Override
    public boolean isSatisfied(@NotNull Player player) {
        if (!enabled) return true;
        return player.hasPermission(permission);
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
                Pair.of("permission", permission),
                Pair.of("enabled", enabled)
        );
    }

    @Override
    public Component denyMessage() {
        return configManager.getMessage("no-permission-mine");
    }

    public static IMineRequirement deserialize(ConfigManager configManager, JsonObject json) {
        PermissionMineRequirement requirement =
                new PermissionMineRequirement(configManager, json.get("permission").getAsString());

        if (json.has("enabled")) {
            requirement.setEnabled(json.get("enabled").getAsBoolean());
        }

        return requirement;
    }
}