package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.managers.SelectionManager;
import net.kyori.adventure.text.Component;
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

    public SelectionListener(@NotNull SelectionManager selectionManager){
        this.selectionManager = selectionManager;
    }


    @EventHandler
    public void onSelectEvent(PlayerInteractEvent event){
        if (event.getPlayer().hasPermission("simplymines.select")){
            Player player = event.getPlayer();
            if (player.getInventory().getItemInMainHand().getType() != Material.WOODEN_HOE) return;
            Block block = event.getClickedBlock();
            if (block == null) return;
            Location location = block.getLocation();
            if (event.getAction().isLeftClick()) {
                event.setCancelled(true);
                selectionManager.setCorener(player.getUniqueId(), location, 1);
                //TODO: Change to config
                player.sendMessage(Component.text("Selected Corner 1 at "
                        + location.getX() + " " + location.getY() + " " + location.getZ()));
            }
            if (event.getAction().isRightClick()) {
                event.setCancelled(true);
                selectionManager.setCorener(player.getUniqueId(), location, 2);
                //TODO: Change to config
                player.sendMessage(Component.text("Selected Corner 2 at "
                        + location.getX() + " " + location.getY() + " " + location.getZ()));

            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e){
        selectionManager.deleteCorners(e.getPlayer().getUniqueId());
    }

}
