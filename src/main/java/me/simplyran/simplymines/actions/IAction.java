package me.simplyran.simplymines.actions;

import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IAction {

    void perform(@NotNull Location location,
                 @NotNull BasicMine mine,
                 @NotNull Player player);

}
