package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
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

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.requirements.min-efficiency.title", "Min Efficiency");
    private final ConfigData<String> adjustRemoveLevels = ConfigFactory.newConfigData(
            "menus.requirements.min-efficiency.adjust-remove-levels", "Remove <amount> Level");
    private final ConfigData<String> adjustAddLevels = ConfigFactory.newConfigData(
            "menus.requirements.min-efficiency.adjust-add-levels", "Add <amount> Level");
    private final ConfigData<String> displayName = ConfigFactory.newConfigData(
            "menus.requirements.min-efficiency.display-name", "<white>Required Efficiency Level");
    private final ConfigData<String> displayLore = ConfigFactory.newConfigData(
            "menus.requirements.min-efficiency.display-lore", "<white>Level <level>");
    private final ConfigData<String> toggleLabel = ConfigFactory.newConfigData(
            "menus.requirements.min-efficiency.toggle-label", "Min Efficiency");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;
    private final ConfigManager configManager;

    public MinEfficiencyGUI(ConfigManager configManager,
                            SimplyMines plugin,
                            MineManager mineManager,
                            GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
        this.configManager = configManager;

        configManager.registerLang(title);
        configManager.registerLang(adjustRemoveLevels);
        configManager.registerLang(adjustAddLevels);
        configManager.registerLang(displayName);
        configManager.registerLang(displayLore);
        configManager.registerLang(toggleLabel);
    }

    public void open(Player player, BasicMine mine) {
        EfficiencyMineRequirement req = getOrCreate(configManager, mine);

        Gui gui = Gui.gui().rows(3).title(MessageUtils.format(title)).disableAllInteractions().create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineRequirementsGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        new ToggleButton(gui, 1, 5, MessageUtils.plainFormat(toggleLabel), req::isEnabled, req::setEnabled, null).render();
        renderDisplay(gui, req);

        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, adjustRemoveLevels, NamedTextColor.RED, delta -> adjust(gui, req, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, adjustRemoveLevels, NamedTextColor.RED, delta -> adjust(gui, req, -delta)).render();
        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, adjustAddLevels, NamedTextColor.GREEN, delta -> adjust(gui, req, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, adjustAddLevels, NamedTextColor.GREEN, delta -> adjust(gui, req, delta)).render();

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
                        .name(MessageUtils.format(displayName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(displayLore, "level", String.valueOf(req.getEfficiencyLevel()))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

}
