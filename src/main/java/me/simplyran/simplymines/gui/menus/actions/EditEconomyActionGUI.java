package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.impl.EconomyAction;
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
 * Editor for a single {@link EconomyAction} instance attached to a block.
 */
public class EditEconomyActionGUI {

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public EditEconomyActionGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    public void open(Player player, String block, BasicMine mine, EconomyAction action) {
        Gui gui = Gui.gui()
                .rows(4)
                .title(Component.text("Edit Economy Action"))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockActionsGUI().open(player, block, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(4, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(1, 7,
                ItemBuilder.from(Material.BARRIER)
                        .name(Component.text("Remove Action").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED))
                        .asGuiItem(event -> {
                            mine.removeAction(block, action);
                            mineManager.saveMineAsync(mine);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockActionsGUI().open(player, block, mine));
                        }));

        renderDisplay(gui, action);

        // Amount adjust row
        new AdjustButton(gui, 2, 2, Material.RED_DYE, 100, "Remove 100 Amount", NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 10, "Remove 10 Amount", NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1 Amount", NamedTextColor.RED,
                delta -> adjustAmount(gui, action, -delta)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1 Amount", NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 10, "Add 10 Amount", NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 100, "Add 100 Amount", NamedTextColor.GREEN,
                delta -> adjustAmount(gui, action, delta)).render();

        // Chance adjust row
        new AdjustButton(gui, 3, 2, Material.RED_DYE, 10, "Remove 10% Chance", NamedTextColor.RED,
                delta -> adjustChance(gui, action, -delta / 100.0)).render();
        new AdjustButton(gui, 3, 3, Material.RED_DYE, 5, "Remove 5% Chance", NamedTextColor.RED,
                delta -> adjustChance(gui, action, -delta / 100.0)).render();
        new AdjustButton(gui, 3, 4, Material.RED_DYE, 1, "Remove 1% Chance", NamedTextColor.RED,
                delta -> adjustChance(gui, action, -delta / 100.0)).render();

        new AdjustButton(gui, 3, 6, Material.LIME_DYE, 1, "Add 1% Chance", NamedTextColor.GREEN,
                delta -> adjustChance(gui, action, delta / 100.0)).render();
        new AdjustButton(gui, 3, 7, Material.LIME_DYE, 5, "Add 5% Chance", NamedTextColor.GREEN,
                delta -> adjustChance(gui, action, delta / 100.0)).render();
        new AdjustButton(gui, 3, 8, Material.LIME_DYE, 10, "Add 10% Chance", NamedTextColor.GREEN,
                delta -> adjustChance(gui, action, delta / 100.0)).render();

        gui.open(player);
    }

    private void adjustAmount(Gui gui, EconomyAction action, double delta) {
        action.setAmount(Math.max(0, action.getAmount() + delta));
        renderDisplay(gui, action);
        gui.update();
    }

    private void adjustChance(Gui gui, EconomyAction action, double delta) {
        action.setChance(action.getChance() + delta);
        renderDisplay(gui, action);
        gui.update();
    }

    private void renderDisplay(Gui gui, EconomyAction action) {
        int chancePercent = (int) Math.round(action.getChance() * 100);
        gui.setItem(1, 5,
                ItemBuilder.from(Material.GOLD_INGOT)
                        .name(Component.text("Amount: " + action.getAmount()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .lore(Component.text("Chance: " + chancePercent + "%").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem());
    }
}
