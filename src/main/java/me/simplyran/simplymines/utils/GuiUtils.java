package me.simplyran.simplymines.utils;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.BaseGui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;

public final class GuiUtils {

    private GuiUtils() {}

    /**
     * Fills the full border (top row, bottom row, left/right columns) with white glass panes.
     * */
    public static void fillBorder(BaseGui gui) {
        fillBorder(gui, Material.WHITE_STAINED_GLASS_PANE);
    }

    /**
     * Fills the full border with the given material.
     * */
    public static void fillBorder(BaseGui gui, Material material) {
        fillBorder(gui, borderItem(material));
    }

    /**
     * Fills the full border with a custom pre-built GuiItem (e.g. a filler with a name/lore).
     * */
    public static void fillBorder(BaseGui gui, GuiItem item) {
        int rows = gui.getRows();

        // Top and bottom rows (all 9 columns)
        for (int col = 1; col <= 9; col++) {
            gui.setItem(1, col, item);
            gui.setItem(rows, col, item);
        }

        // Left and right columns for the rows in between (top/bottom already covered)
        for (int row = 2; row < rows; row++) {
            gui.setItem(row, 1, item);
            gui.setItem(row, 9, item);
        }
    }

    /**
    * Fills only the top and bottom rows (useful for pagination bars).
    * **/
    public static void fillTopAndBottom(BaseGui gui, Material material) {
        GuiItem item = borderItem(material);
        int rows = gui.getRows();
        for (int col = 1; col <= 9; col++) {
            gui.setItem(1, col, item);
            gui.setItem(rows, col, item);
        }
    }

    /**
     * Fills an entire single row.
     * */
    public static void fillRow(BaseGui gui, int row, Material material) {
        GuiItem item = borderItem(material);
        for (int col = 1; col <= 9; col++) {
            gui.setItem(row, col, item);
        }
    }

    private static GuiItem borderItem(Material material) {
        return ItemBuilder.from(material)
                .name(Component.text(" ")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem();
    }
}