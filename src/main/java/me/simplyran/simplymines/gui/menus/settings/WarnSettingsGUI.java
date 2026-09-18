package me.simplyran.simplymines.gui.menus.settings;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
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
 * Hub menu linking to Warn Seconds and Warn Distance settings.
 */
public class WarnSettingsGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.settings.warn-settings.title", "Warn Settings");
    private final ConfigData<String> warnSecondsName = ConfigFactory.newConfigData(
            "menus.settings.warn-settings.warn-seconds-name", "<yellow>Warn Seconds");
    private final ConfigData<String> warnDistanceName = ConfigFactory.newConfigData(
            "menus.settings.warn-settings.warn-distance-name", "<yellow>Warn Distance");
    private final ConfigData<String> warnDistanceLore = ConfigFactory.newConfigData(
            "menus.settings.warn-settings.warn-distance-lore", "<white><amount> Blocks");

    private final SimplyMines plugin;
    private final GuiManager guiManager;
    private final MineManager mineManager;

    public WarnSettingsGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.mineManager = mineManager;

        configManager.registerLang(title);
        configManager.registerLang(warnSecondsName);
        configManager.registerLang(warnDistanceName);
        configManager.registerLang(warnDistanceLore);
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
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineEditorGUI().open(player, mine.getName()));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(2, 4,
                ItemBuilder.from(Material.REDSTONE_TORCH)
                        .name(MessageUtils.format(warnSecondsName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> guiManager.getWarnSecondsGUI().open(player, mine)));

        gui.setItem(2, 6,
                ItemBuilder.from(Material.SPYGLASS)
                        .name(MessageUtils.format(warnDistanceName)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(warnDistanceLore, "amount", String.valueOf(mine.getWarnDistance()))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> guiManager.getWarnDistanceGUI().open(player, mine)));

        gui.open(player);
    }
}
