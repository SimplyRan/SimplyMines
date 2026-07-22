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
}
