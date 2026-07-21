package me.simplyran.simplymines.actions.impl;

import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemDropAction implements IAction {

    private final ItemStack itemStack;

    public ItemDropAction(@NotNull ItemStack itemStack){
        this.itemStack = itemStack.clone();
    }

    @Override
    public void perform(@NotNull Location location,
                        @NotNull BasicMine mine,
                        @NotNull Player player) {

        if (mine.isAutoPickup()) player.getInventory().addItem(itemStack.clone());
        else location.getWorld().dropItem(location, itemStack.clone());
    }
}
