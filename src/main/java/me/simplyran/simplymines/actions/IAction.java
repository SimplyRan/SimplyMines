package me.simplyran.simplymines.actions;

import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IAction {

    void perform(@NotNull Location location,
                 @NotNull BasicMine mine,
                 @NotNull Player player);

    String name();

    List<Pair<String, Object>> serialize();

    /** Chance (0.0-1.0) that this action fires when its block is triggered. */
    double getChance();

    void setChance(double chance);

    /**
     * Rolls this action's chance and performs it if the roll succeeds.
     * Returns whether the action actually fired.
     */
    default boolean activate(@NotNull Location location,
                             @NotNull BasicMine mine,
                             @NotNull Player player) {
        if (Math.random() > getChance()) return false;
        perform(location, mine, player);
        return true;
    }
}
