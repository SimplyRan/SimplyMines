package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.events.BlockBrokenInMineEvent;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BlockBreakListener implements Listener {

    private final MineManager mineManager;
    private final ConfigManager configManager;
    private final SimplyMines plugin;

    public BlockBreakListener(@NotNull MineManager mineManager,
                              @NotNull ConfigManager configManager,
                              @NotNull SimplyMines plugin){
        this.mineManager = mineManager;
        this.configManager = configManager;
        this.plugin = plugin;
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Location location = e.getBlock().getLocation();
        Player player = e.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        for (BasicMine mine : mineManager.getMines()){

            if (mine.isInsideMine(location)){

                if (!mine.meetsEfficiencyRequirement(tool)) {
                    e.setCancelled(true);
                    //TODO Change to config
                    player.sendMessage(configManager.getMessage("higher-efficiency-level", "<level>", Integer.toString(mine.getMinEfficiency())));
                    return;
                }

                plugin.getServer().getPluginManager().callEvent(
                        new BlockBrokenInMineEvent(mine,
                                player,
                                e.getBlock())
                );
            }
        }
    }

    @EventHandler
    public void onMineBlockEvent(BlockBrokenInMineEvent e){
        e.getMine().addBlockBroken();
    }



}
