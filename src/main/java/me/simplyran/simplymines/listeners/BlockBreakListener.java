package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.events.BlockBrokenImMineEvent;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class BlockBreakListener implements Listener {

    private final MineManager mineManager;
    private final SimplyMines plugin;

    public BlockBreakListener(@NotNull MineManager mineManager,
                              @NotNull SimplyMines plugin){
        this.mineManager = mineManager;
        this.plugin = plugin;
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        Location location  = e.getBlock().getLocation();
        for (BasicMine mine : mineManager.getMines()){

            if (mine.isInsideMine(location)){

                plugin.getServer().getPluginManager().callEvent(
                        new BlockBrokenImMineEvent(mine,
                                e.getPlayer(),
                                e.getBlock())
                );

            }
        }
    }

    @EventHandler
    public void onMineBlockEvent(BlockBrokenImMineEvent e){
        e.getMine().addBlockBroken();
    }



}
