package me.simplyran.simplymines.gui.menus.settings;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Menu for adjusting a mine's warn distance via +/- buttons.
 */
public class WarnDistanceGUI {

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public WarnDistanceGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Change Warn Distance"))
                .disableAllInteractions()
                .create();

        // Go back to warn settings hub, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getWarnSettingsGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderDisplay(gui, mine);

        // Remove buttons
        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10 blocks from Warn Distance", NamedTextColor.RED,
                delta -> adjust(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5 blocks from Warn Distance", NamedTextColor.RED,
                delta -> adjust(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1 block from Warn Distance", NamedTextColor.RED,
                delta -> adjust(gui, mine, -delta)).render();

        // Add buttons
        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1 block to Warn Distance", NamedTextColor.GREEN,
                delta -> adjust(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5 blocks to Warn Distance", NamedTextColor.GREEN,
                delta -> adjust(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10 blocks to Warn Distance", NamedTextColor.GREEN,
                delta -> adjust(gui, mine, delta)).render();

        gui.open(player);
    }

    private void adjust(Gui gui, BasicMine mine, int delta) {
        int newWarnDistance = Math.max(0, mine.getWarnDistance() + delta);
        mine.setWarnDistance(newWarnDistance);
        renderDisplay(gui, mine);
        gui.update();
    }

    private void renderDisplay(Gui gui, BasicMine mine) {
        gui.setItem(2, 5,
                ItemBuilder.from(Material.COMPASS)
                        .name(Component.text("Warn Distance")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(mine.getWarnDistance() + " Blocks")
                                .color(NamedTextColor.WHITE)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }
}