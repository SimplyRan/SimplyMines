package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.events.BlockBrokenInMineEvent;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
        Location location = e.getBlock().getLocation();
        Player player = e.getPlayer();

        for (BasicMine mine : mineManager.getMines()){
            if (!mine.isInsideMine(location)) continue;

            for (IMineRequirement mineRequirement : mine.getMineRequirements()){
                if (!mineRequirement.isSatisfied(player)){
                    player.sendMessage(mineRequirement.denyMessage());

                    e.setCancelled(true);
                    //Stop after it
                    return;
                }
            }

            plugin.getServer().getPluginManager().callEvent(
                    new BlockBrokenInMineEvent(mine,
                            player,
                            e.getBlock())
            );

        }
    }

    @EventHandler
    public void onMineBlockEvent(BlockBrokenInMineEvent e){
        e.getMine().addBlockBroken();
    }



}
