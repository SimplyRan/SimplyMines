package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.utils.ChatInputManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatInputListener implements Listener {

    private final SimplyMines plugin;

    public ChatInputListener(SimplyMines plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!ChatInputManager.hasPending(player)) return;

        event.setCancelled(true);
        String message = event.getMessage();
        Bukkit.getScheduler().runTask(plugin, () -> ChatInputManager.handleInput(player, message));
    }
}