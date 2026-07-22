package me.simplyran.simplymines.actions.impl;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EconomyAction implements IAction {

    public final static String NAME = "economy_action";
    private final double amount;

    public EconomyAction(double amount){
        this.amount = amount;
    }


    //TODO Implement
    @Override
    public void perform(@NotNull Location location, @NotNull BasicMine mine, @NotNull Player player) {

    }

    @Override
    public String name() {
        return NAME;
    }


    @Override
    public List<Pair<String, Object>> serialize() {
        return List.of(Pair.of("amount", amount));
    }

    public static IAction deserialize(@NotNull JsonObject json) {
        double amount = 0;
        if (json.has("amount")){
            amount = json.get("amount").getAsDouble();
        }

        return new EconomyAction(amount);
    }


}
