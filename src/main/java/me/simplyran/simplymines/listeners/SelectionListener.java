package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.SelectionManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class SelectionListener implements Listener {

    private final SelectionManager selectionManager;
    private final ConfigManager configManager;

    public SelectionListener(@NotNull SelectionManager selectionManager, @NotNull ConfigManager configManager){
        this.selectionManager = selectionManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onSelectEvent(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if (player.hasPermission("simplymines.select") && !selectionManager.isToolDisabled(player.getUniqueId())){


            if (player.getInventory().getItemInMainHand().getType() != Material.WOODEN_HOE) return;
            Block block = event.getClickedBlock();
            if (block == null) return;
            Location location = block.getLocation();

            String x = String.valueOf(location.getX());
            String y = String.valueOf(location.getY());
            String z = String.valueOf(location.getZ());

            if (event.getAction().isLeftClick()) {
                event.setCancelled(true);
                selectionManager.setCorener(player.getUniqueId(), location, 1);
                player.sendMessage(configManager.getMessage("selected-corner-1",
                        "x", x, "y", y, "z", z));
            }
            if (event.getAction().isRightClick()) {
                event.setCancelled(true);
                selectionManager.setCorener(player.getUniqueId(), location, 2);
                player.sendMessage(configManager.getMessage("selected-corner-2",
                        "x", x, "y", y, "z", z));
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e){
        selectionManager.deleteCorners(e.getPlayer().getUniqueId());
    }

}