package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.events.BlockBrokenInMineEvent;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
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
        Block block = e.getBlock();

        String blockID = ItemUtils.getIDFromBlock(block);

        for (BasicMine mine : mineManager.getMines()){
            if (!mine.isEnabled()) continue;
            if (!mine.isInsideMine(location)) continue;
            if (!mine.isNormalDropsEnabled() && e.isDropItems()) e.setDropItems(false);

            for (IMineRequirement mineRequirement : mine.getMineRequirements()){
                if (!mineRequirement.isSatisfied(player)){
                    player.sendMessage(mineRequirement.denyMessage());

                    e.setCancelled(true);
                    //Stop after it
                    return;
                }
            }

            for (IAction action : mine.getActions(blockID)){
                action.activate(location, mine, player);
            }

            plugin.getServer().getPluginManager().callEvent(
                    new BlockBrokenInMineEvent(mine,
                            player,
                            block)
            );



        }
    }

    @EventHandler
    public void onMineBlockEvent(BlockBrokenInMineEvent e){
        e.getMine().addBlockBroken();
    }



}
