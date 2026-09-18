package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AddResetRequirementGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.requirements.add-reset-requirement.title", "Add Reset Requirement");
    private final ConfigData<String> timeResetUnavailable = ConfigFactory.newConfigData(
            "menus.requirements.add-reset-requirement.time-reset-unavailable", "<red>Time Reset (Already Added)");
    private final ConfigData<String> timeResetName = ConfigFactory.newConfigData(
            "menus.requirements.add-reset-requirement.time-reset-name", "<yellow>Time Reset");
    private final ConfigData<String> timeResetLore = ConfigFactory.newConfigData(
            "menus.requirements.add-reset-requirement.time-reset-lore", "<gray>Resets on a fixed timer");
    private final ConfigData<String> percentResetUnavailable = ConfigFactory.newConfigData(
            "menus.requirements.add-reset-requirement.percent-reset-unavailable", "<red>Percent Reset (Already Added)");
    private final ConfigData<String> percentResetName = ConfigFactory.newConfigData(
            "menus.requirements.add-reset-requirement.percent-reset-name", "<yellow>Percent Reset");
    private final ConfigData<String> percentResetLore = ConfigFactory.newConfigData(
            "menus.requirements.add-reset-requirement.percent-reset-lore", "<gray>Resets once enough of the mine is broken");

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public AddResetRequirementGUI(ConfigManager configManager, SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(timeResetUnavailable);
        configManager.registerLang(timeResetName);
        configManager.registerLang(timeResetLore);
        configManager.registerLang(percentResetUnavailable);
        configManager.registerLang(percentResetName);
        configManager.registerLang(percentResetLore);
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(MessageUtils.format(title))
                .disableAllInteractions()
                .create();

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> guiManager.getResetRequirementsGUI().open(player, mine)));

        boolean hasTime = mine.getResetRequirement(TimeResetRequirement.class) != null;
        boolean hasPercent = mine.getResetRequirement(PercentResetRequirement.class) != null;

        gui.setItem(2, 3, hasTime
                ? unavailable(timeResetUnavailable)
                : ItemBuilder.from(Material.CLOCK)
                .name(MessageUtils.format(timeResetName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .lore(MessageUtils.format(timeResetLore).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem(event -> {
                    mine.addResetRequirement(new TimeResetRequirement(30));
                    Bukkit.getScheduler().runTask(plugin, () -> guiManager.getResetRequirementsGUI().open(player, mine));
                }));

        gui.setItem(2, 5, hasPercent
                ? unavailable(percentResetUnavailable)
                : ItemBuilder.from(Material.REPEATER)
                .name(MessageUtils.format(percentResetName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .lore(MessageUtils.format(percentResetLore).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem(event -> {
                    PercentResetRequirement req = new PercentResetRequirement(mine, 10.0);
                    req.setEnabled(true);
                    mine.addResetRequirement(req);
                    Bukkit.getScheduler().runTask(plugin, () -> guiManager.getResetRequirementsGUI().open(player, mine));
                }));

        gui.open(player);
    }

    private GuiItem unavailable(ConfigData<String> name) {
        return ItemBuilder.from(Material.BARRIER)
                .name(MessageUtils.format(name).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem();
    }
}
