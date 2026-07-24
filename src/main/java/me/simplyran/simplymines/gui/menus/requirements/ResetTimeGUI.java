package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Menu for adjusting a mine's timed reset interval via +/- buttons.
 */
public class ResetTimeGUI {

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public ResetTimeGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        TimeResetRequirement req = getOrCreate(mine);

        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Change Reset Time"))
                .disableAllInteractions()
                .create();

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

        renderDisplay(gui, req);

        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10 seconds from Reset Time", NamedTextColor.RED,
                delta -> adjust(gui, req, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5 seconds from Reset Time", NamedTextColor.RED,
                delta -> adjust(gui, req, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1 second from Reset Time", NamedTextColor.RED,
                delta -> adjust(gui, req, -delta)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1 second to Reset Time", NamedTextColor.GREEN,
                delta -> adjust(gui, req, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5 seconds to Reset Time", NamedTextColor.GREEN,
                delta -> adjust(gui, req, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10 seconds to Reset Time", NamedTextColor.GREEN,
                delta -> adjust(gui, req, delta)).render();

        gui.open(player);
    }

    private TimeResetRequirement getOrCreate(BasicMine mine) {
        TimeResetRequirement req = mine.getResetRequirement(TimeResetRequirement.class);
        if (req == null) {
            req = new TimeResetRequirement(mine, 30);
            mine.addResetRequirement(req);
        }
        return req;
    }

    private void adjust(Gui gui, TimeResetRequirement req, int delta) {
        int newResetTime = Math.max(1, req.getResetTime() + delta);
        req.setResetTime(newResetTime);
        renderDisplay(gui, req);
        gui.update();
    }

    private void renderDisplay(Gui gui, TimeResetRequirement req) {
        gui.setItem(2, 5,
                ItemBuilder.from(Material.CLOCK)
                        .name(Component.text("Reset Time").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(Component.text(req.getResetTime() + "s").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

}