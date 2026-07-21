package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.jetbrains.annotations.NotNull;

public class BlockDropItemListener implements Listener {

    private final MineManager mineManager;

    public BlockDropItemListener(@NotNull MineManager mineManager){
        this.mineManager = mineManager;
    }


    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        Location location = event.getBlock().getLocation();
        Player player = event.getPlayer();
        for (BasicMine mine : mineManager.getMines()){
            if (mine.isInsideMine(location) && mine.isAutoPickup()){
                for (Item item : event.getItems()){
                    player.getInventory().addItem(item.getItemStack());
                    item.remove();
                }
                break;
            }
        }
    }


}
