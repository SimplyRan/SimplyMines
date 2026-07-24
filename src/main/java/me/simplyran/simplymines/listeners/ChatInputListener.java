package me.simplyran.simplymines.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.utils.ChatInputManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatInputListener implements Listener {

    private final SimplyMines plugin;

    public ChatInputListener(SimplyMines plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (!ChatInputManager.hasPending(player)) return;

        event.setCancelled(true);
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        Bukkit.getScheduler().runTask(plugin, () -> ChatInputManager.handleInput(player, message));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ChatInputManager.cancel(event.getPlayer());
    }
}
