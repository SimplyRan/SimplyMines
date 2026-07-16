package me.simplyran.simplymines.managers;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final SimplyMines plugin;
    private final Map<String, String> messages = new HashMap<>();

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    // Only used by getMessageLegacy(), for any call sites that still need a plain
    // legacy-formatted String instead of an Adventure Component.
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    public ConfigManager(@NotNull SimplyMines plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadMessages();
    }

    private void loadMessages() {
        messages.clear();

        FileConfiguration config = plugin.getConfig();
        config.addDefault("messages.only-players", "<red>Only players can use this command.");
        config.addDefault("messages.no-permission", "<red>You do not have permission to use this command.");
        config.addDefault("messages.no-permission-reload", "<red>You do not have permission to reload.");
        config.addDefault("messages.reloaded", "<green>Mines and Config have been reloaded!");
        config.addDefault("messages.unknown-subcommand", "<red>Unknown subcommand: <yellow><input>");
        config.addDefault("messages.usage", "<red>Usage: /<label> [reload|reset|create|delete|disable|enable] [mine name]");

        config.addDefault("messages.no-permission-reset", "<red>You do not have permission to reset mines.");
        config.addDefault("messages.mine-reset", "<green>Mine <mine> has been reset!");
        config.addDefault("messages.mine-not-found", "<red>Mine <mine> not found!");

        config.addDefault("messages.no-permission-create", "<red>You do not have permission to create mines.");
        config.addDefault("messages.only-players-create", "<red>Only players can create mines.");
        config.addDefault("messages.mine-already-exists", "<red>Mine <mine> already exists.");
        config.addDefault("messages.no-selection", "<red>No Selection found! Use a Wooden Hoe to select 2 corners.");

        config.addDefault("messages.no-permission-delete", "<red>You do not have permission to delete mines.");
        config.addDefault("messages.mine-deleted", "<green>Mine <mine> has been deleted.");

        config.addDefault("messages.no-permission-disable", "<red>You do not have permission to disable mines.");
        config.addDefault("messages.mine-disabled", "<red>Disabled <mine>.");

        config.addDefault("messages.no-permission-enable", "<red>You do not have permission to enable mines.");
        config.addDefault("messages.mine-enabled", "<green>Enabled <mine>.");
        config.addDefault("messages.no-permission-enable", "<red>You do not have permission to enable mines.");
        config.addDefault("messages.mine-enabled", "<green>Enabled <mine>.");

        config.addDefault("messages.selected-corner-1", "<green>Selected Corner 1 at <x>, <y>, <z>");
        config.addDefault("messages.selected-corner-2", "<green>Selected Corner 2 at <x>, <y>, <z>");

        config.addDefault("messages.no-permission-move", "<red>You do not have permission to move mines.");
        config.addDefault("messages.mine-moved", "<green>Mine <mine> has been moved!");

        config.addDefault("messages.warn-global", "<yellow><mine> <gray>resets in <red><seconds>s<gray> (server-wide)!");
        config.addDefault("messages.warn-near", "<yellow><mine> <gray>resets nearby in <red><seconds>s<gray>!");

        config.addDefault("messages.only-players-can-tool", "<green>Only players can use tool");
        config.addDefault("messages.disabled-tool", "<red>Disabled tool.");
        config.addDefault("messages.enabled-tool", "<green>Enabled tool.");
        config.addDefault("messages.no-permission-tool", "<red>no permission to toggle tool.");

        config.addDefault("messages.missing-mine-name", "<red>You need to specify a mine name!");

        config.addDefault("messages.higher-efficiency-level", "<red>You need a tool with Efficiency <level> or higher to mine here.");

        config.addDefault("max_workload", 20_000_000);
        config.addDefault("save_mines_seconds", 1800);


        config.options().copyDefaults(true);
        plugin.saveConfig();

        WorkloadRunnable.setMAX_WORKLOADS(config.getInt("max_workload"));
        RunnableManager.setSAVE_MINES_FILES(config.getInt("save_mines_seconds"));

        for (String key : config.getConfigurationSection("messages").getKeys(false)) {
            messages.put(key, config.getString("messages." + key, ""));
        }
    }

    /**
     * Gets the raw MiniMessage string for a key, with no parsing or placeholder substitution.
     */
    public String getRaw(@NotNull String key) {
        return messages.getOrDefault(key, key);
    }

    /**
     * Parses a message into an Adventure Component with no placeholders.
     * Send it directly: player.sendMessage(component) / sender.sendMessage(component)
     * (works out of the box on Paper; on Spigot, send it through a BukkitAudiences instance).
     */
    public Component getMessage(@NotNull String key) {
        return MINI_MESSAGE.deserialize(getRaw(key));
    }

    /**
     * Parses a message into a Component, substituting a single placeholder.
     * The placeholder name may be given with or without surrounding '%' so old
     * call sites (e.g. "%mine%") keep working alongside the new MiniMessage tag style ("mine").
     */
    public Component getMessage(@NotNull String key,
                                @NotNull String placeholder,
                                @NotNull String value) {
        return MINI_MESSAGE.deserialize(getRaw(key), Placeholder.unparsed(normalize(placeholder), value));
    }

    /**
     * Parses a message into a Component, substituting multiple placeholders.
     * Pairs must be passed as placeholder, value, placeholder, value, ...
     */
    public Component getMessage(@NotNull String key, @NotNull String... placeholderValuePairs) {
        TagResolver.Builder resolvers = TagResolver.builder();
        for (int i = 0; i + 1 < placeholderValuePairs.length; i += 2) {
            resolvers.resolver(Placeholder.unparsed(normalize(placeholderValuePairs[i]), placeholderValuePairs[i + 1]));
        }
        return MINI_MESSAGE.deserialize(getRaw(key), resolvers.build());
    }

    /**
     * Legacy-formatted String variant (colors translated with '&' section codes),
     * for any code path that can't yet accept an Adventure Component.
     * Prefer the Component-returning overloads above wherever possible.
     */
    public String getMessageLegacy(@NotNull String key, @NotNull String... placeholderValuePairs) {
        Component component = placeholderValuePairs.length == 0
                ? getMessage(key)
                : getMessage(key, placeholderValuePairs);
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * Strips leading/trailing '%' from a placeholder name so both "%mine%"
     * and "mine" resolve to the same MiniMessage tag <mine>.
     */
    private String normalize(@NotNull String placeholder) {
        String result = placeholder;
        if (result.startsWith("%")) result = result.substring(1);
        if (result.endsWith("%")) result = result.substring(0, result.length() - 1);
        return result;
    }
}