package me.simplyran.simplymines.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.simplyran.simplymines.objects.ConfigData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MessageUtils() {}

    public static Component format(@NotNull ConfigData<String> data, @NotNull String... placeholderValuePairs) {
        return format(null, data, placeholderValuePairs);
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
        return MINI_MESSAGE.deserialize(raw, resolvers.build());
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
