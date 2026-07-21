package me.simplyran.simplymines.actions.impl;

import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemDropAction implements IAction {

    private final ItemStack itemStack;

    //TODO when impl insta goes to inv go through here
    public ItemDropAction(@NotNull ItemStack itemStack){
        this.itemStack = itemStack.clone();
    }

    @Override
    public void perform(@NotNull Location location,
                        @NotNull BasicMine mine,
                        @NotNull Player player) {
        location.getWorld().dropItem(location, itemStack.clone());

    }
}
