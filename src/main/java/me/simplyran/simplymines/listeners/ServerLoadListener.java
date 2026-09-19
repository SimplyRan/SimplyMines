package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.smelting.SmeltRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerLoadListener implements Listener {

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        SmeltRegistry.rebuild();
    }
}
