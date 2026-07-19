package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.objects.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
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

    private final ConfigData<String> selectedCorner1 = ConfigFactory.newConfigData(
            "messages.selected-corner-1", "<green>Selected Corner 1 at <x>, <y>, <z>");
    private final ConfigData<String> selectedCorner2 = ConfigFactory.newConfigData(
            "messages.selected-corner-2", "<green>Selected Corner 2 at <x>, <y>, <z>");

    public SelectionListener(@NotNull SelectionManager selectionManager, @NotNull ConfigManager configManager){
        this.selectionManager = selectionManager;
        configManager.register(selectedCorner1);
        configManager.register(selectedCorner2);
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
                player.sendMessage(MessageUtils.format(player, selectedCorner1,
                        "x", x, "y", y, "z", z));
            }
            if (event.getAction().isRightClick()) {
                event.setCancelled(true);
                selectionManager.setCorener(player.getUniqueId(), location, 2);
                player.sendMessage(MessageUtils.format(player, selectedCorner2,
                        "x", x, "y", y, "z", z));
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e){
        selectionManager.deleteCorners(e.getPlayer().getUniqueId());
    }

}
