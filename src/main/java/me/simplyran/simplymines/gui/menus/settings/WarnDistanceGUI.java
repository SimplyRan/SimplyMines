package me.simplyran.simplymines.gui.menus.settings;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
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

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.settings.warn-distance.title", "Change Warn Distance");
    private final ConfigData<String> warnDistanceName = ConfigFactory.newConfigData(
            "menus.settings.warn-distance.warn-distance-name", "<white>Warn Distance");
    private final ConfigData<String> warnDistanceLore = ConfigFactory.newConfigData(
            "menus.settings.warn-distance.warn-distance-lore", "<white><amount> Blocks");
    private final ConfigData<String> removeTenLabel = ConfigFactory.newConfigData(
            "menus.settings.warn-distance.remove-ten-label", "Remove 10 blocks from Warn Distance");
    private final ConfigData<String> removeFiveLabel = ConfigFactory.newConfigData(
            "menus.settings.warn-distance.remove-five-label", "Remove 5 blocks from Warn Distance");
    private final ConfigData<String> removeOneLabel = ConfigFactory.newConfigData(
            "menus.settings.warn-distance.remove-one-label", "Remove 1 block from Warn Distance");
    private final ConfigData<String> addOneLabel = ConfigFactory.newConfigData(
            "menus.settings.warn-distance.add-one-label", "Add 1 block to Warn Distance");
    private final ConfigData<String> addFiveLabel = ConfigFactory.newConfigData(
            "menus.settings.warn-distance.add-five-label", "Add 5 blocks to Warn Distance");
    private final ConfigData<String> addTenLabel = ConfigFactory.newConfigData(
            "menus.settings.warn-distance.add-ten-label", "Add 10 blocks to Warn Distance");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public WarnDistanceGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(warnDistanceName);
        configManager.registerLang(warnDistanceLore);
        configManager.registerLang(removeTenLabel);
        configManager.registerLang(removeFiveLabel);
        configManager.registerLang(removeOneLabel);
        configManager.registerLang(addOneLabel);
        configManager.registerLang(addFiveLabel);
        configManager.registerLang(addTenLabel);
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(MessageUtils.format(title))
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
                        .name(MessageUtils.format(MenuCommonText.BACK)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        renderDisplay(gui, mine);

        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, removeTenLabel, NamedTextColor.RED,
                delta -> adjust(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, removeFiveLabel, NamedTextColor.RED,
                delta -> adjust(gui, mine, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, removeOneLabel, NamedTextColor.RED,
                delta -> adjust(gui, mine, -delta)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, addOneLabel, NamedTextColor.GREEN,
                delta -> adjust(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, addFiveLabel, NamedTextColor.GREEN,
                delta -> adjust(gui, mine, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, addTenLabel, NamedTextColor.GREEN,
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
                        .name(MessageUtils.format(warnDistanceName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(warnDistanceLore, "amount", String.valueOf(mine.getWarnDistance()))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }
}
