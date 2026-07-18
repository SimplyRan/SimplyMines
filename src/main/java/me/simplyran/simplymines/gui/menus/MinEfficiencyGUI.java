package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
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
 * Menu for adjusting/toggling a mine's minimum required tool-efficiency level.
 */
public class MinEfficiencyGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;
    private final ConfigManager configManager;

    public MinEfficiencyGUI(ConfigManager configManager,
                            SimplyMines plugin,
                            GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.configManager = configManager;
    }

    public void open(Player player, BasicMine mine) {
        EfficiencyMineRequirement req = getOrCreate(configManager, mine);

        Gui gui = Gui.gui().rows(3).title(Component.text("Min Efficiency")).disableAllInteractions().create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            MineSaver.saveAsync(plugin, mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineRequirementsGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderToggle(gui, req);
        renderDisplay(gui, req);

        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5 Levels", NamedTextColor.RED, delta -> adjust(gui, req, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1 Level", NamedTextColor.RED, delta -> adjust(gui, req, -delta)).render();
        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1 Level", NamedTextColor.GREEN, delta -> adjust(gui, req, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5 Levels", NamedTextColor.GREEN, delta -> adjust(gui, req, delta)).render();

        gui.open(player);
    }

    private EfficiencyMineRequirement getOrCreate(ConfigManager configManager, BasicMine mine) {
        EfficiencyMineRequirement req = mine.getMineRequirement(EfficiencyMineRequirement.class);
        if (req == null) {
            req = new EfficiencyMineRequirement(configManager, 0);
            req.setEnabled(false);
            mine.addMineRequirement(req);
        }
        return req;
    }

    private void adjust(Gui gui, EfficiencyMineRequirement req, int delta) {
        int newValue = Math.max(0, req.getEfficiencyLevel() + delta);
        req.setEfficiencyLevel(newValue);
        renderDisplay(gui, req);
        gui.update();
    }

    private void renderDisplay(Gui gui, EfficiencyMineRequirement req) {
        gui.setItem(2, 5,
                ItemBuilder.from(Material.GOLDEN_PICKAXE)
                        .name(Component.text("Required Efficiency Level").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text("Level " + req.getEfficiencyLevel()).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    private void renderToggle(Gui gui, EfficiencyMineRequirement req) {
        boolean enabled = req.isEnabled();
        gui.setItem(1, 5,
                ItemBuilder.from(Material.ENCHANTED_BOOK)
                        .name(Component.text("Min Efficiency: ").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE)
                                .append(Component.text(enabled ? "Enabled" : "Disabled").color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)))
                        .lore(Component.text("Click to toggle").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                        .asGuiItem(event -> {
                            req.setEnabled(!req.isEnabled());
                            renderToggle(gui, req);
                            gui.update();
                        }));
    }

}