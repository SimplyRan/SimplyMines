package me.simplyran.simplymines.utils;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Lets a GUI ask the player to type something in chat and get it back
 * via callback, since inventory GUIs can't take free text input.
 */
public final class ChatInputManager {

    private static final Map<UUID, Consumer<String>> PENDING = new ConcurrentHashMap<>();

    private ChatInputManager() {}

    public static void awaitInput(Player player, Consumer<String> callback) {
        PENDING.put(player.getUniqueId(), callback);
    }

    public static boolean hasPending(Player player) {
        return PENDING.containsKey(player.getUniqueId());
    }

    public static void handleInput(Player player, String message) {
        Consumer<String> callback = PENDING.remove(player.getUniqueId());
        if (callback != null) callback.accept(message);
    }

    public static void cancel(Player player) {
        PENDING.remove(player.getUniqueId());
    }
}