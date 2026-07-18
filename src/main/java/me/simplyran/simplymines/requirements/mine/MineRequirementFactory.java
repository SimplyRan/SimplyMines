package me.simplyran.simplymines.requirements.mine;

import com.google.gson.JsonObject;
import me.simplyran.simplymines.managers.ConfigManager;

@FunctionalInterface
public interface MineRequirementFactory {
    IMineRequirement create(ConfigManager configManager, JsonObject json);
}
