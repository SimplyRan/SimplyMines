package me.simplyran.simplymines.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import me.simplyran.simplymines.objects.ConfigData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Formats lang/config messages.
 * <p>
 * Every {@link ConfigData} message is compiled once (on lang reload, or lazily on first use) into a
 * {@link Template}: MiniMessage colors/formatting are parsed up front and each {@code <placeholder>}
 * tag is left in the component tree as a marker. Formatting at runtime is then only a marker swap,
 * with no MiniMessage parsing. Messages without placeholders are served as the cached component as is.
 */
public class MessageUtils {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Set<String> LOGGED_BAD_KEYS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<ConfigData<String>, Template> TEMPLATES = new ConcurrentHashMap<>();

    // Private-use characters, they never show up in real lang text.
    private static final char MARKER_START = '\uE000';
    private static final char MARKER_END = '\uE001';

    /**
     * These tags spread their colors over the length of the text inside them, so a placeholder inside
     * one can't be precompiled: the marker's length differs from the real value's and the tag splits it
     * into single characters. Such messages keep the plain parse-per-call behavior.
     */
    private static final Pattern LENGTH_DEPENDENT_TAGS =
            Pattern.compile("<\\s*(gradient|rainbow|transition)\\b", Pattern.CASE_INSENSITIVE);

    private MessageUtils() {}

    /**
     * A message parsed ahead of time. {@code raw} is remembered so a changed value invalidates the entry.
     * {@code hasPlaceholders} is false when {@code component} can be returned untouched.
     * {@code fullParse} marks messages that must be parsed on every use, see {@link #LENGTH_DEPENDENT_TAGS}.
     */
    private record Template(String raw, Component component, boolean hasPlaceholders, boolean fullParse) {}

    /** Drops every compiled message. Called before recompiling on a lang reload. */
    public static void clearCache() {
        TEMPLATES.clear();
        LOGGED_BAD_KEYS.clear();
    }

    /** Compiles a message ahead of time so the first real use doesn't pay for parsing. */
    public static void precompile(@NotNull ConfigData<String> data) {
        template(data);
    }

    public static Component format(@NotNull ConfigData<String> data, @NotNull String... placeholderValuePairs) {
        return format(null, data, placeholderValuePairs);
    }

    public static Component format(@Nullable CommandSender sender,
                                   @NotNull String raw,
                                   @NotNull String... placeholderValuePairs) {

        if (sender instanceof Player player && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            raw = PlaceholderAPI.setPlaceholders(player, raw);
        }

        return MINI_MESSAGE.deserialize(raw, buildResolvers(placeholderValuePairs));
    }

    public static Component format(@Nullable CommandSender sender,
                                   @NotNull ConfigData<String> data,
                                   @NotNull String... placeholderValuePairs) {
        String raw = data.getValue();

        // PlaceholderAPI output depends on the player, so these can't be precompiled.
        if (sender instanceof Player player
                && raw.indexOf('%') >= 0
                && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return parse(data, PlaceholderAPI.setPlaceholders(player, raw), buildResolvers(placeholderValuePairs));
        }

        Template template = template(data);
        if (!template.hasPlaceholders()) {
            return template.component();
        }
        if (template.fullParse()) {
            return parse(data, raw, buildResolvers(placeholderValuePairs));
        }
        return substitute(template.component(), placeholderValuePairs);
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

    private static Template template(@NotNull ConfigData<String> data) {
        String raw = data.getValue();
        Template cached = TEMPLATES.get(data);
        if (cached != null && cached.raw().equals(raw)) {
            return cached;
        }

        MarkerResolver markers = new MarkerResolver();
        Component component = parse(data, raw, markers);
        boolean fullParse = markers.found && LENGTH_DEPENDENT_TAGS.matcher(raw).find();
        Template template = new Template(raw, component, markers.found, fullParse);
        TEMPLATES.put(data, template);
        return template;
    }

    private static Component parse(@NotNull ConfigData<String> data,
                                   @NotNull String raw,
                                   @NotNull TagResolver resolvers) {
        try {
            return MINI_MESSAGE.deserialize(raw, resolvers);
        } catch (Exception e) {
            if (LOGGED_BAD_KEYS.add(data.getPath())) {
                Bukkit.getLogger().warning("Invalid MiniMessage formatting for lang key '"
                        + data.getPath() + "': " + e.getMessage());
            }
            try {
                return MINI_MESSAGE.deserialize(data.getDefaultValue(), resolvers);
            } catch (Exception fallbackException) {
                return Component.text(data.getDefaultValue());
            }
        }
    }

    private static TagResolver buildResolvers(@NotNull String[] placeholderValuePairs) {
        TagResolver.Builder resolvers = TagResolver.builder();
        for (int i = 0; i + 1 < placeholderValuePairs.length; i += 2) {
            resolvers.resolver(Placeholder.unparsed(normalize(placeholderValuePairs[i]), placeholderValuePairs[i + 1]));
        }
        return resolvers.build();
    }

    /**
     * Swaps the markers left by {@link MarkerResolver} for the given values. Values are inserted as
     * plain text (same as {@link Placeholder#unparsed}), so surrounding styles apply to them.
     * Placeholders that weren't supplied render as the literal {@code <name>}, like unknown tags do.
     * Returns the same instance when nothing changed, and only rebuilds the parts that did.
     */
    private static Component substitute(@NotNull Component component, @NotNull String[] pairs) {
        boolean changed = false;

        Component result = component;
        if (component instanceof TextComponent text) {
            String content = text.content();
            String replaced = replaceMarkers(content, pairs);
            if (!replaced.equals(content)) {
                result = text.content(replaced);
                changed = true;
            }
        }

        Style style = result.style();
        HoverEvent<?> hover = style.hoverEvent();
        if (hover != null && hover.action() == HoverEvent.Action.SHOW_TEXT && hover.value() instanceof Component hoverText) {
            Component replaced = substitute(hoverText, pairs);
            if (replaced != hoverText) {
                result = result.style(style.hoverEvent(HoverEvent.showText(replaced)));
                changed = true;
            }
        }

        List<Component> children = result.children();
        if (!children.isEmpty()) {
            List<Component> newChildren = null;
            for (int i = 0; i < children.size(); i++) {
                Component child = children.get(i);
                Component replaced = substitute(child, pairs);
                if (replaced != child && newChildren == null) {
                    newChildren = new ArrayList<>(children);
                }
                if (newChildren != null) {
                    newChildren.set(i, replaced);
                }
            }
            if (newChildren != null) {
                result = result.children(newChildren);
                changed = true;
            }
        }

        return changed ? result : component;
    }

    private static String replaceMarkers(@NotNull String content, @NotNull String[] pairs) {
        int start = content.indexOf(MARKER_START);
        if (start < 0) return content;

        StringBuilder out = new StringBuilder(content.length() + 16);
        int last = 0;
        while (start >= 0) {
            int end = content.indexOf(MARKER_END, start + 1);
            if (end < 0) break;

            String name = content.substring(start + 1, end);
            out.append(content, last, start).append(valueFor(name, pairs));
            last = end + 1;
            start = content.indexOf(MARKER_START, last);
        }
        return out.append(content, last, content.length()).toString();
    }

    private static String valueFor(@NotNull String name, @NotNull String[] pairs) {
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            if (normalize(pairs[i]).equalsIgnoreCase(name)) {
                return pairs[i + 1];
            }
        }
        return "<" + name + ">";
    }

    private static String normalize(@NotNull String placeholder) {
        String result = placeholder;
        if (result.startsWith("%")) result = result.substring(1);
        if (result.endsWith("%")) result = result.substring(0, result.length() - 1);
        return result;
    }

    /**
     * Claims every tag MiniMessage doesn't know as a standard tag and inserts a marker for it,
     * which is how placeholder positions get recorded at compile time.
     */
    private static final class MarkerResolver implements TagResolver {

        private static final TagResolver STANDARD = TagResolver.standard();

        private boolean found = false;

        @Override
        public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) {
            if (!has(name)) return null;
            found = true;
            return Tag.inserting(Component.text(MARKER_START + name + MARKER_END));
        }

        @Override
        public boolean has(@NotNull String name) {
            return !name.isEmpty() && !STANDARD.has(name) && name.chars().allMatch(MarkerResolver::isNameChar);
        }

        private static boolean isNameChar(int c) {
            return (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-';
        }
    }
}
