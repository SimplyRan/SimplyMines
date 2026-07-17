package me.simplyran.simplymines.gui.buttons;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.function.IntConsumer;

/**
 * A "+N / -N" style button that nudges some value up or down and
 * fires a refresh callback (so a linked display item can be redrawn).
 */
public class AdjustButton {

    private final BaseGui gui;
    private final int row;
    private final int col;
    private final Material material;
    private final int amount;      // used as item stack count, purely visual
    private final String label;
    private final NamedTextColor color;
    private final IntConsumer onClick; // receives the delta to apply

    public AdjustButton(BaseGui gui, int row, int col, Material material,
                        int amount, String label, NamedTextColor color,
                        IntConsumer onClick) {
        this.gui = gui;
        this.row = row;
        this.col = col;
        this.material = material;
        this.amount = amount;
        this.label = label;
        this.color = color;
        this.onClick = onClick;
    }

    public void render() {
        gui.setItem(row, col, ItemBuilder.from(material)
                .amount(Math.max(1, Math.min(amount, 64)))
                .name(Component.text(label)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(color))
                .asGuiItem(event -> onClick.accept(amount)));
    }
}