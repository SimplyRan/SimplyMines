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

/**
 * All of a mine's on/off settings in one place, grouped by topic,
 * so the editor hub stays a clean navigation menu.
 */
public class MineSettingsGUI {

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public MineSettingsGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(5)
                .title(Component.text("Settings: " + mine.getName()))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineEditorGUI().open(player, mine.getName()));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(5, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        sectionLabel(gui, 2, 2, Material.NETHER_STAR, "General");
        new ToggleButton(gui, 2, 4, "Mine Enabled",
                "Master switch - disabled mines never reset.",
                mine::isEnabled, mine::setEnabled, save(mine)).render();
        new ToggleButton(gui, 2, 6, "Teleport Players",
                "Teleport players out of the mine before a reset.",
                mine::isTeleportPlayers, mine::setTeleportPlayers, save(mine)).render();

        sectionLabel(gui, 3, 2, Material.IRON_PICKAXE, "Mining");
        new ToggleButton(gui, 3, 4, "Normal Drops",
                "Broken blocks drop their vanilla items.",
                mine::isNormalDropsEnabled, mine::setNormalDropsEnabled, save(mine)).render();
        new ToggleButton(gui, 3, 6, "Auto Pickup",
                "Drops go straight into the player's inventory.",
                mine::isAutoPickup, mine::setAutoPickup, save(mine)).render();
        new ToggleButton(gui, 3, 8, "Fortune",
                "Fortune on the tool multiplies item drops.",
                mine::isFortuneEnabled, mine::setFortuneEnabled, save(mine)).render();

        sectionLabel(gui, 4, 2, Material.TNT, "Reset & Warnings");
        new ToggleButton(gui, 4, 4, "Replace Mode",
                "Reset overwrites every block, not just air.",
                mine::isReplaceMode, mine::setReplaceMode, save(mine)).render();
        new ToggleButton(gui, 4, 5, "Use Physics",
                "Placed blocks trigger physics updates.",
                mine::isUsePhysics, mine::setUsePhysics, save(mine)).render();
        new ToggleButton(gui, 4, 7, "Warn Global",
                "Broadcast reset warnings to the whole server.",
                mine::isWarnGlobal, mine::setWarnGlobal, save(mine)).render();
        new ToggleButton(gui, 4, 8, "Warn Near",
                "Warn only players near the mine before a reset.",
                mine::isWarnNear, mine::setWarnNear, save(mine)).render();

        gui.open(player);
    }

    private Runnable save(BasicMine mine) {
        return () -> mineManager.saveMineAsync(mine);
    }

    private void sectionLabel(Gui gui, int row, int col, Material icon, String label) {
        gui.setItem(row, col,
                ItemBuilder.from(icon)
                        .name(Component.text(label)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .decorate(TextDecoration.BOLD)
                                .color(NamedTextColor.GOLD))
                        .asGuiItem());
    }
}
