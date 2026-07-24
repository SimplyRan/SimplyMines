package me.simplyran.simplymines.gui.buttons;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * A self-rendering on/off button for TriumphGUI menus.
 * Owns its own row/col so it can never drift out of sync with where it's drawn.
 */
public class ToggleButton {

    private final BaseGui gui;
    private final int row;
    private final int col;
    private final String label;
    private final String description; // may be null
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private final Runnable onToggle; // e.g. save-to-disk, may be null

    public ToggleButton(BaseGui gui, int row, int col, String label,
                        BooleanSupplier getter, Consumer<Boolean> setter,
                        Runnable onToggle) {
        this(gui, row, col, label, null, getter, setter, onToggle);
    }

    public ToggleButton(BaseGui gui, int row, int col, String label, String description,
                        BooleanSupplier getter, Consumer<Boolean> setter,
                        Runnable onToggle) {
        this.gui = gui;
        this.row = row;
        this.col = col;
        this.label = label;
        this.description = description;
        this.getter = getter;
        this.setter = setter;
        this.onToggle = onToggle;
    }

    /** Draws (or redraws) the button at its slot based on current state. */
    public void render() {
        boolean state = getter.getAsBoolean();
        Material material = state ? Material.LIME_DYE : Material.RED_DYE;

        List<Component> lore = new ArrayList<>();
        if (description != null) {
            lore.add(Component.text(description)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .color(NamedTextColor.GRAY));
        }
        lore.add(Component.text("Click to toggle")
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .color(NamedTextColor.DARK_GRAY));

        gui.setItem(row, col, ItemBuilder.from(material)
                .name(Component.text(label + ": ")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text(state ? "Enabled" : "Disabled")
                                .color(state ? NamedTextColor.GREEN : NamedTextColor.RED)))
                .lore(lore)
                .asGuiItem(event -> toggle()));
    }

    private void toggle() {
        setter.accept(!getter.getAsBoolean());
        render();
        gui.update();
        if (onToggle != null) onToggle.run();
    }
}