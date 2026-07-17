package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MineSaver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Menu for adjusting/toggling a mine's reset-at-percentage threshold.
 */
public class ResetPercentageGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public ResetPercentageGUI(SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Reset At Percentage"))
                .disableAllInteractions()
                .create();

        // Go back to reset settings hub, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            MineSaver.saveAsync(plugin, mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getResetSettingsGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderToggle(gui, mine);
        renderDisplay(gui, mine);

        // Remove buttons
        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10% from Reset Threshold", NamedTextColor.RED,
                delta -> adjust(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5% from Reset Threshold", NamedTextColor.RED,
                delta -> adjust(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1% from Reset Threshold", NamedTextColor.RED,
                delta -> adjust(gui, mine, -delta)).render();

        // Add buttons
        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1% to Reset Threshold", NamedTextColor.GREEN,
                delta -> adjust(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5% to Reset Threshold", NamedTextColor.GREEN,
                delta -> adjust(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10% to Reset Threshold", NamedTextColor.GREEN,
                delta -> adjust(gui, mine, delta)).render();

        gui.open(player);
    }

    private void adjust(Gui gui, BasicMine mine, double delta) {
        double newValue = mine.getResetAtPercentage() + delta;
        newValue = Math.max(0, Math.min(100, newValue));
        mine.setResetAtPercentage(newValue);
        renderDisplay(gui, mine);
        gui.update();
    }

    private void renderDisplay(Gui gui, BasicMine mine) {
        gui.setItem(2, 5,
                ItemBuilder.from(Material.COMPARATOR)
                        .name(Component.text("Reset Threshold")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getResetAtPercentage() + "% left")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    private void renderToggle(Gui gui, BasicMine mine) {
        boolean enabled = mine.isResetAtPercentageEnabled();
        gui.setItem(1, 5,
                ItemBuilder.from(Material.REPEATER)
                        .name(Component.text("Reset At Percentage: ")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE)
                                .append(Component.text(enabled ? "Enabled" : "Disabled")
                                        .color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)))
                        .lore(Component.text("Click to toggle")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.GRAY))
                        .asGuiItem(event -> {
                            mine.setResetAtPercentageEnabled(!mine.isResetAtPercentageEnabled());
                            renderToggle(gui, mine);
                            gui.update();
                        }));
    }
}