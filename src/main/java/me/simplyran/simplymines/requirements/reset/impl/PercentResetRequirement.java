package me.simplyran.simplymines.requirements.reset.impl;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PercentResetRequirement implements IResetRequirement {

    public final static String NAME = "percent_reset_requirement";

   private final BasicMine mine;
   private boolean enabled;
    // reset at percentage
   @Getter @Setter double resetAtPercentage;


   public PercentResetRequirement(@NotNull BasicMine mine,
                                  double resetAtPercentage){
       this.mine = mine;
       this.resetAtPercentage = resetAtPercentage;
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
    public boolean isSatisfied() {
        return mine.getPercentageOfMineLeft() <= resetAtPercentage;
    }

    @Override
    public void update() {
       //no need here
    }

    @Override
    public List<Pair<String, Object>> serialize() {
        return List.of(
                Pair.of("reset_percentage", resetAtPercentage),
                Pair.of("enabled", enabled)
        );
    }

    public static IResetRequirement deserialize(BasicMine mine, JsonObject json) {
        PercentResetRequirement requirement =
                new PercentResetRequirement(
                        mine,
                        json.get("reset_percentage").getAsDouble()
                );

        if (json.has("enabled")) {
            requirement.setEnabled(json.get("enabled").getAsBoolean());
        }

        return requirement;
    }


}
