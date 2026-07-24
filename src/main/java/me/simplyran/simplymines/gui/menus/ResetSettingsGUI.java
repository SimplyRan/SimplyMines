package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
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
 * Hub menu linking to the Timed and Percentage-based reset settings.
 */
public class ResetSettingsGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;
    private final MineManager mineManager;

    public ResetSettingsGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.mineManager = mineManager;
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Reset Settings"))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineEditorGUI().open(player, mine.getName()));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(2, 4,
                ItemBuilder.from(Material.CLOCK)
                        .name(Component.text("Reset Time (Timed)").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                        .lore(Component.text(resetTimeLabel(mine)).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> guiManager.getResetTimeGUI().open(player, mine)));

        gui.setItem(2, 6,
                ItemBuilder.from(Material.REPEATER)
                        .name(Component.text("Reset At Percentage").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                        .lore(Component.text(percentageLabel(mine)).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(percentEnabled(mine) ? NamedTextColor.GREEN : NamedTextColor.RED))
                        .asGuiItem(event -> guiManager.getResetPercentageGUI().open(player, mine)));

        gui.open(player);
    }

    private String resetTimeLabel(BasicMine mine) {
        TimeResetRequirement req = mine.getResetRequirement(TimeResetRequirement.class);
        return req != null ? req.getResetTime() + "s" : "Not set";
    }

    private boolean percentEnabled(BasicMine mine) {
        PercentResetRequirement req = mine.getResetRequirement(PercentResetRequirement.class);
        return req != null && req.isEnabled();
    }

    private String percentageLabel(BasicMine mine) {
        PercentResetRequirement req = mine.getResetRequirement(PercentResetRequirement.class);
        if (req == null || !req.isEnabled()) return "Disabled";
        return req.getResetAtPercentage() + "% left (Enabled)";
    }
}