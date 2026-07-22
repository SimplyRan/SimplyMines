package me.simplyran.simplymines.actions.impl;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EconomyAction implements IAction {

    public final static String NAME = "economy_action";
    @Setter @Getter private double amount;
    private double chance;

    public EconomyAction(double amount){
        this(amount, 1.0);
    }

    public EconomyAction(double amount, double chance){
        this.amount = amount;
        this.chance = chance;
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
    public double getChance() {
        return chance;
    }

    @Override
    public void setChance(double chance) {
        this.chance = Math.clamp(chance, 0, 1);
    }


    @Override
    public List<Pair<String, Object>> serialize() {
        return List.of(Pair.of("amount", amount), Pair.of("chance", chance));
    }

    public static IAction deserialize(@NotNull JsonObject json) {
        double amount = 0;
        if (json.has("amount")){
            amount = json.get("amount").getAsDouble();
        }

        double chance = json.has("chance") ? json.get("chance").getAsDouble() : 1.0;

        return new EconomyAction(amount, chance);
    }


}
