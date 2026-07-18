package me.simplyran.simplymines.requirements.reset;

import com.google.gson.JsonObject;
import me.simplyran.simplymines.objects.BasicMine;

@FunctionalInterface
public interface ResetRequirementFactory {
    IResetRequirement create(BasicMine mine, JsonObject json);
}