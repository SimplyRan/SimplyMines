package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
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
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public ResetPercentageGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        PercentResetRequirement req = getOrCreate(mine);

        Gui gui = Gui.gui().rows(3).title(Component.text("Reset At Percentage")).disableAllInteractions().create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getResetRequirementsGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderToggle(gui, req);
        renderDisplay(gui, req);

        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10% from Reset Threshold", NamedTextColor.RED, delta -> adjust(gui, req, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5% from Reset Threshold", NamedTextColor.RED, delta -> adjust(gui, req, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1% from Reset Threshold", NamedTextColor.RED, delta -> adjust(gui, req, -delta)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1% to Reset Threshold", NamedTextColor.GREEN, delta -> adjust(gui, req, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5% to Reset Threshold", NamedTextColor.GREEN, delta -> adjust(gui, req, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10% to Reset Threshold", NamedTextColor.GREEN, delta -> adjust(gui, req, delta)).render();

        gui.open(player);
    }

    private PercentResetRequirement getOrCreate(BasicMine mine) {
        PercentResetRequirement req = mine.getResetRequirement(PercentResetRequirement.class);
        if (req == null) {
            req = new PercentResetRequirement(mine, 10.0);
            req.setEnabled(false);
            mine.addResetRequirement(req);
        }
        return req;
    }

    private void adjust(Gui gui, PercentResetRequirement req, double delta) {
        double newValue = Math.clamp(req.getResetAtPercentage() + delta, 0, 100);
        req.setResetAtPercentage(newValue);
        renderDisplay(gui, req);
        gui.update();
    }

    private void renderDisplay(Gui gui, PercentResetRequirement req) {
        gui.setItem(2, 5,
                ItemBuilder.from(Material.COMPARATOR)
                        .name(Component.text("Reset Threshold").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(req.getResetAtPercentage() + "% left").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

    private void renderToggle(Gui gui, PercentResetRequirement req) {
        boolean enabled = req.isEnabled();
        gui.setItem(1, 5,
                ItemBuilder.from(Material.REPEATER)
                        .name(Component.text("Reset At Percentage: ").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE)
                                .append(Component.text(enabled ? "Enabled" : "Disabled").color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)))
                        .lore(Component.text("Click to toggle").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY))
                        .asGuiItem(event -> {
                            req.setEnabled(!req.isEnabled());
                            renderToggle(gui, req);
                            gui.update();
                        }));
    }

}