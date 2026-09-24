package me.simplyran.simplymines.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.simplyran.simplymines.objects.ConfigData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MessageUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Set<String> LOGGED_BAD_KEYS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private MessageUtils() {}

    public static Component format(@NotNull ConfigData<String> data, @NotNull String... placeholderValuePairs) {
        return format(null, data, placeholderValuePairs);
    }

    public static Component format(@Nullable CommandSender sender,
                                   @NotNull String raw,
                                   @NotNull String... placeholderValuePairs) {

        if (sender instanceof Player player && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            raw = PlaceholderAPI.setPlaceholders(player, raw);
        }

        TagResolver.Builder resolvers = TagResolver.builder();
        for (int i = 0; i + 1 < placeholderValuePairs.length; i += 2) {
            resolvers.resolver(Placeholder.unparsed(normalize(placeholderValuePairs[i]), placeholderValuePairs[i + 1]));
        }

        TagResolver builtResolvers = resolvers.build();

        return MINI_MESSAGE.deserialize(raw, builtResolvers);
    }


    public static Component format(@Nullable CommandSender sender,
                                   @NotNull ConfigData<String> data,
                                   @NotNull String... placeholderValuePairs) {
        String raw = data.getValue();

        if (sender instanceof Player player && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            raw = PlaceholderAPI.setPlaceholders(player, raw);
        }

        TagResolver.Builder resolvers = TagResolver.builder();
        for (int i = 0; i + 1 < placeholderValuePairs.length; i += 2) {
            resolvers.resolver(Placeholder.unparsed(normalize(placeholderValuePairs[i]), placeholderValuePairs[i + 1]));
        }

        TagResolver builtResolvers = resolvers.build();

        try {
            return MINI_MESSAGE.deserialize(raw, builtResolvers);
        } catch (Exception e) {
            if (LOGGED_BAD_KEYS.add(data.getPath())) {
                Bukkit.getLogger().warning("Invalid MiniMessage formatting for lang key '"
                        + data.getPath() + "': " + e.getMessage());
            }
            try {
                return MINI_MESSAGE.deserialize(data.getDefaultValue(), builtResolvers);
            } catch (Exception fallbackException) {
                return Component.text(data.getDefaultValue());
            }
        }
    }

    public static String plainFormat(@NotNull ConfigData<String> data, @NotNull String... placeholderValuePairs) {
        return PlainTextComponentSerializer.plainText().serialize(format(data, placeholderValuePairs));
    }

    public static String applyPlaceholders(@NotNull Player player, @NotNull String raw) {
        String result = raw.replace("%player%", player.getName());

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }

        return result;
    }

    private static String normalize(@NotNull String placeholder) {
        String result = placeholder;
        if (result.startsWith("%")) result = result.substring(1);
        if (result.endsWith("%")) result = result.substring(0, result.length() - 1);
        return result;
    }
}
