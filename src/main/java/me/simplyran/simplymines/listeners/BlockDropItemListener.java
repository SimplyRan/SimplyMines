package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.smelting.SmeltRegistry;
import me.simplyran.simplymines.utils.DropUtils;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockDropItemListener implements Listener {

    private final MineManager mineManager;

    public BlockDropItemListener(@NotNull MineManager mineManager){
        this.mineManager = mineManager;
    }


    @EventHandler(ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        Location location = event.getBlock().getLocation();
        Player player = event.getPlayer();

        BasicMine mine = findMine(location);
        if (mine == null) return;

        boolean smelt = mine.canAutoSmelt(player);
        boolean pickup = mine.canAutoPickup(player);
        if (!smelt && !pickup) return;

        List<Item> items = event.getItems();

        if (pickup) {
            List<ItemStack> stacks = new ArrayList<>(items.size());
            for (Item item : items) {
                ItemStack stack = item.getItemStack();
                ItemStack smelted = smelt ? SmeltRegistry.smelt(stack) : null;
                stacks.add(smelted != null ? smelted : stack);
            }

            items.clear();
            for (ItemStack stack : stacks) {
                DropUtils.giveOrDrop(player, location, stack);
            }
            return;
        }

        for (Item item : items) {
            ItemStack smelted = SmeltRegistry.smelt(item.getItemStack());
            if (smelted == null) continue;

            List<ItemStack> parts = DropUtils.splitByMaxStack(smelted);
            item.setItemStack(parts.get(0));
            for (ItemStack extra : parts.subList(1, parts.size())) {
                DropUtils.dropAt(item.getLocation(), extra);
            }
        }
    }

    @Nullable
    private BasicMine findMine(Location location) {
        for (BasicMine mine : mineManager.getMines()) {
            if (mine.isEnabled() && mine.isInsideMine(location)) return mine;
        }
        return null;
    }

}
