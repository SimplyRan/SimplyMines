package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MineSaver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Menu for adjusting a single block's drop chance within a mine.
 */
public class EditBlockGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public EditBlockGUI(SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void open(Player player, String block, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .disableAllInteractions()
                .title(Component.text("Edit Block Chances"))
                .create();

        // Go back to blocks GUI, but only on a genuine player-initiated close
        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            MineSaver.saveAsync(plugin, mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlocksGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderBlockDisplay(gui, block, mine);
        renderAllBlocksLore(gui, mine);

        // Remove buttons
        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10%", NamedTextColor.RED,
                delta -> adjust(gui, block, mine, -delta / 100.0)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5%", NamedTextColor.RED,
                delta -> adjust(gui, block, mine, -delta / 100.0)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1%", NamedTextColor.RED,
                delta -> adjust(gui, block, mine, -delta / 100.0)).render();

        // Add buttons
        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1%", NamedTextColor.GREEN,
                delta -> adjust(gui, block, mine, delta / 100.0)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5%", NamedTextColor.GREEN,
                delta -> adjust(gui, block, mine, delta / 100.0)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10%", NamedTextColor.GREEN,
                delta -> adjust(gui, block, mine, delta / 100.0)).render();

        gui.open(player);
    }

    /** Adds (or subtracts, if negative) `delta` fraction to a block's chance, clamped sanely. */
    private void adjust(Gui gui, String block, BasicMine mine, double delta) {
        double oldPercent = mine.getPercentage(block);
        double newPercent;

        if (delta < 0) {
            newPercent = Math.max(0, oldPercent + delta);
        } else {
            double totalWithoutCurrent = mine.getTotalPercentage() - oldPercent;
            newPercent = oldPercent + delta;
            if (totalWithoutCurrent + newPercent > 1.0) {
                newPercent = 1.0 - totalWithoutCurrent;
            }
            newPercent = Math.max(0, Math.min(1, newPercent));
        }

        mine.setPercentage(block, newPercent);
        renderBlockDisplay(gui, block, mine);
        renderAllBlocksLore(gui, mine);
        gui.update();
    }

    private void renderBlockDisplay(Gui gui, String block, BasicMine mine) {
        double percent = mine.getPercentage(block);
        gui.setItem(2, 5,
                ItemBuilder.from(ItemUtils.getItemStackFromName(block))
                        .name(Component.text("Block Percent: " + Math.round(percent * 100) + "%")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    private void renderAllBlocksLore(Gui gui, BasicMine mine) {
        List<Component> lore = new ArrayList<>();
        for (Map.Entry<String, Double> materials : mine.getMaterials()) {
            lore.add(Component.text("   " + materials.getKey() +
                            ": " + Math.round(materials.getValue() * 100) + "%")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        gui.setItem(1, 5,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(Component.text("All Blocks: ")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.AQUA))
                        .lore(lore)
                        .asGuiItem());
    }
}