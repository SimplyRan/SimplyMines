package me.simplyran.simplymines.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class DropUtils {

    private DropUtils() {}

    @NotNull
    public static List<ItemStack> splitByMaxStack(@NotNull ItemStack stack) {
        List<ItemStack> parts = new ArrayList<>();
        int maxStack = Math.max(1, stack.getMaxStackSize());
        int remaining = stack.getAmount();

        while (remaining > 0) {
            ItemStack part = stack.clone();
            part.setAmount(Math.min(remaining, maxStack));
            parts.add(part);
            remaining -= part.getAmount();
        }

        return parts;
    }

    public static void giveOrDrop(@NotNull Player player, @NotNull Location location, @NotNull ItemStack stack) {
        World world = location.getWorld();

        for (ItemStack part : splitByMaxStack(stack)) {
            for (ItemStack leftover : player.getInventory().addItem(part).values()) {
                if (world != null) world.dropItem(location, leftover);
            }
        }
    }

    public static void dropAt(@NotNull Location location, @NotNull ItemStack stack) {
        World world = location.getWorld();
        if (world == null) return;

        for (ItemStack part : splitByMaxStack(stack)) {
            world.dropItem(location, part);
        }
    }
}
