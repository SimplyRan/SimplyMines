package me.simplyran.simplymines.gui.menus.settings;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
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

import java.util.List;

/**
 * Menu for toggling which warn-second thresholds are active for a mine.
 */
public class WarnSecondsGUI {

    private static final int[] WARN_SECOND_OPTIONS = {1, 2, 5, 10, 15, 30, 60};

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public WarnSecondsGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Warn Seconds"))
                .disableAllInteractions()
                .create();

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

        List<Integer> warnSec = mine.getWarnSeconds();

        int col = 2;
        for (int seconds : WARN_SECOND_OPTIONS) {
            new ToggleButton(gui, 2, col, seconds + " Warn Seconds",
                    () -> warnSec.contains(seconds),
                    enabled -> {
                        if (enabled) {
                            if (!warnSec.contains(seconds)) warnSec.add(seconds);
                        } else {
                            warnSec.remove(Integer.valueOf(seconds));
                        }
                    },
                    null
            ).render();
            col++;
        }

        gui.open(player);
    }
}