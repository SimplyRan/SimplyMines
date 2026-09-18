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
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
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

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.settings.reset-settings.title", "Reset Settings");
    private final ConfigData<String> resetTimeName = ConfigFactory.newConfigData(
            "menus.settings.reset-settings.reset-time-name", "<yellow>Reset Time (Timed)");
    private final ConfigData<String> resetTimeLore = ConfigFactory.newConfigData(
            "menus.settings.reset-settings.reset-time-lore", "<white><value>");
    private final ConfigData<String> resetPercentageName = ConfigFactory.newConfigData(
            "menus.settings.reset-settings.reset-percentage-name", "<yellow>Reset At Percentage");
    private final ConfigData<String> resetPercentageEnabledLore = ConfigFactory.newConfigData(
            "menus.settings.reset-settings.reset-percentage-enabled-lore", "<green><percent>% left (Enabled)");
    private final ConfigData<String> resetPercentageDisabledLore = ConfigFactory.newConfigData(
            "menus.settings.reset-settings.reset-percentage-disabled-lore", "<red>Disabled");

    private final SimplyMines plugin;
    private final GuiManager guiManager;
    private final MineManager mineManager;

    public ResetSettingsGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.mineManager = mineManager;

        configManager.registerLang(title);
        configManager.registerLang(resetTimeName);
        configManager.registerLang(resetTimeLore);
        configManager.registerLang(resetPercentageName);
        configManager.registerLang(resetPercentageEnabledLore);
        configManager.registerLang(resetPercentageDisabledLore);
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
                ItemBuilder.from(Material.CLOCK)
                        .name(MessageUtils.format(resetTimeName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(resetTimeLore(mine))
                        .asGuiItem(event -> guiManager.getResetTimeGUI().open(player, mine)));

        gui.setItem(2, 6,
                ItemBuilder.from(Material.REPEATER)
                        .name(MessageUtils.format(resetPercentageName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(percentageLore(mine))
                        .asGuiItem(event -> guiManager.getResetPercentageGUI().open(player, mine)));

        gui.open(player);
    }

    private Component resetTimeLore(BasicMine mine) {
        TimeResetRequirement req = mine.getResetRequirement(TimeResetRequirement.class);
        Component value = req != null
                ? MessageUtils.format(resetTimeLore, "value", req.getResetTime() + "s")
                : MessageUtils.format(MenuCommonText.NOT_SET).colorIfAbsent(NamedTextColor.WHITE);
        return value.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private boolean percentEnabled(BasicMine mine) {
        PercentResetRequirement req = mine.getResetRequirement(PercentResetRequirement.class);
        return req != null && req.isEnabled();
    }

    private Component percentageLore(BasicMine mine) {
        PercentResetRequirement req = mine.getResetRequirement(PercentResetRequirement.class);
        Component value = percentEnabled(mine)
                ? MessageUtils.format(resetPercentageEnabledLore, "percent", String.valueOf(req.getResetAtPercentage()))
                : MessageUtils.format(resetPercentageDisabledLore);
        return value.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
