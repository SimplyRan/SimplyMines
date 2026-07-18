package me.simplyran.simplymines.requirements.mine.impl;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EfficiencyMineRequirement implements IMineRequirement {

    public final static String NAME = "efficiency_mine_requirement";
    private boolean enabled;
    @Getter @Setter int efficiencyLevel;

    public EfficiencyMineRequirement(int efficiencyLevel){
        this.efficiencyLevel = efficiencyLevel;
    }

    @Override
    public boolean isSatisfied(@NotNull Player player) {
        return player.getInventory()
                .getItemInMainHand()
                .getEnchantmentLevel(Enchantment.EFFICIENCY) >= efficiencyLevel;
    }

    @Override
    public boolean isEnabled(){
        return enabled;
    }


    @Override
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }


    @Override
    public List<Pair<String, Object>> serialize() {
        return List.of(Pair.of("efficiency_level", efficiencyLevel));
    }

    public static IMineRequirement deserialize(JsonObject json) {
        EfficiencyMineRequirement requirement =
                new EfficiencyMineRequirement(
                        json.get("efficiency_level").getAsInt()
                );

        if (json.has("enabled")) {
            requirement.setEnabled(json.get("enabled").getAsBoolean());
        }

        return requirement;
    }
}
