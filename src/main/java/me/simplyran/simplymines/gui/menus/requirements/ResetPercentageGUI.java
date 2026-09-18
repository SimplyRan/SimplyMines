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
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
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

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.requirements.reset-percentage.title", "Reset At Percentage");
    private final ConfigData<String> adjustRemove = ConfigFactory.newConfigData(
            "menus.requirements.reset-percentage.adjust-remove", "Remove <amount>% from Reset Threshold");
    private final ConfigData<String> adjustAdd = ConfigFactory.newConfigData(
            "menus.requirements.reset-percentage.adjust-add", "Add <amount>% to Reset Threshold");
    private final ConfigData<String> displayName = ConfigFactory.newConfigData(
            "menus.requirements.reset-percentage.display-name", "<white>Reset Threshold");
    private final ConfigData<String> displayLore = ConfigFactory.newConfigData(
            "menus.requirements.reset-percentage.display-lore", "<white><percent>% left");
    private final ConfigData<String> toggleLabel = ConfigFactory.newConfigData(
            "menus.requirements.reset-percentage.toggle-label", "Reset At Percentage");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public ResetPercentageGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(adjustRemove);
        configManager.registerLang(adjustAdd);
        configManager.registerLang(displayName);
        configManager.registerLang(displayLore);
        configManager.registerLang(toggleLabel);
    }

    public void open(Player player, BasicMine mine) {
        PercentResetRequirement req = getOrCreate(mine);

        Gui gui = Gui.gui().rows(3).title(MessageUtils.format(title)).disableAllInteractions().create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getResetRequirementsGUI().open(player, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        new ToggleButton(gui, 1, 5, MessageUtils.plainFormat(toggleLabel), req::isEnabled, req::setEnabled, null).render();
        renderDisplay(gui, req);

        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, adjustRemove, NamedTextColor.RED, delta -> adjust(gui, req, -delta)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, adjustRemove, NamedTextColor.RED, delta -> adjust(gui, req, -delta)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, adjustRemove, NamedTextColor.RED, delta -> adjust(gui, req, -delta)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, adjustAdd, NamedTextColor.GREEN, delta -> adjust(gui, req, delta)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, adjustAdd, NamedTextColor.GREEN, delta -> adjust(gui, req, delta)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, adjustAdd, NamedTextColor.GREEN, delta -> adjust(gui, req, delta)).render();

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
                        .name(MessageUtils.format(displayName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(displayLore, "percent", String.valueOf(req.getResetAtPercentage()))
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem());
    }

}
