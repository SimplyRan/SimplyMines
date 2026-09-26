package me.simplyran.simplymines.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Fluent builder for menu items; strips default italics from every name and lore line. */
public final class Btn {

    private final ItemBuilder builder;
    private final Component name;
    private final List<Component> lore = new ArrayList<>();
    private TextColor color;
    private Consumer<InventoryClickEvent> onClick;

    private Btn(ItemBuilder builder, Component name) {
        this.builder = builder;
        this.name = name;
    }

    public static Btn of(Material material, ConfigData<String> name, String... placeholders) {
        return new Btn(ItemBuilder.from(material), MessageUtils.format(name, placeholders));
    }

    public static Btn of(Material material, Component name) {
        return new Btn(ItemBuilder.from(material), name);
    }

    /** Keeps the item's own display name. */
    public static Btn of(ItemStack item) {
        return new Btn(ItemBuilder.from(item), null);
    }

    public static Btn of(ItemStack item, Component name) {
        return new Btn(ItemBuilder.from(item), name);
    }

    public Btn color(TextColor color) {
        this.color = color;
        return this;
    }

    public Btn amount(int amount) {
        builder.amount(Math.clamp(amount, 1, 64));
        return this;
    }

    public Btn lore(ConfigData<String> line, String... placeholders) {
        return lore(MessageUtils.format(line, placeholders));
    }

    public Btn lore(Component line) {
        lore.add(noItalic(line));
        return this;
    }

    public Btn lore(List<Component> lines) {
        lines.forEach(this::lore);
        return this;
    }

    public Btn onClick(Consumer<InventoryClickEvent> onClick) {
        this.onClick = onClick;
        return this;
    }

    public GuiItem build() {
        if (name != null) {
            builder.name(noItalic(color == null ? name : name.colorIfAbsent(color)));
        }
        if (!lore.isEmpty()) builder.lore(lore);
        return onClick == null ? builder.asGuiItem() : builder.asGuiItem(onClick::accept);
    }

    private static Component noItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
