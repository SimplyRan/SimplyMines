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
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AddMineRequirementGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.requirements.add-mine-requirement.title", "Add Mine Requirement");
    private final ConfigData<String> minEfficiencyUnavailable = ConfigFactory.newConfigData(
            "menus.requirements.add-mine-requirement.min-efficiency-unavailable", "<red>Min Efficiency (Already Added)");
    private final ConfigData<String> minEfficiencyName = ConfigFactory.newConfigData(
            "menus.requirements.add-mine-requirement.min-efficiency-name", "<yellow>Min Efficiency");
    private final ConfigData<String> minEfficiencyLore = ConfigFactory.newConfigData(
            "menus.requirements.add-mine-requirement.min-efficiency-lore", "<gray>Requires a minimum tool efficiency level");
    private final ConfigData<String> permissionUnavailable = ConfigFactory.newConfigData(
            "menus.requirements.add-mine-requirement.permission-unavailable", "<red>Permission (Already Added)");
    private final ConfigData<String> permissionName = ConfigFactory.newConfigData(
            "menus.requirements.add-mine-requirement.permission-name", "<yellow>Permission");
    private final ConfigData<String> permissionLore = ConfigFactory.newConfigData(
            "menus.requirements.add-mine-requirement.permission-lore", "<gray>Requires a permission node");

    private final SimplyMines plugin;
    private final GuiManager guiManager;
    private final ConfigManager configManager;

    public AddMineRequirementGUI(ConfigManager configManager, SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.configManager = configManager;

        configManager.registerLang(title);
        configManager.registerLang(minEfficiencyUnavailable);
        configManager.registerLang(minEfficiencyName);
        configManager.registerLang(minEfficiencyLore);
        configManager.registerLang(permissionUnavailable);
        configManager.registerLang(permissionName);
        configManager.registerLang(permissionLore);
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
                        .asGuiItem(event -> guiManager.getMineRequirementsGUI().open(player, mine)));

        boolean hasEfficiency = mine.getMineRequirement(EfficiencyMineRequirement.class) != null;
        boolean hasPermission = mine.getMineRequirement(PermissionMineRequirement.class) != null;

        gui.setItem(2, 3, hasEfficiency
                ? unavailable(minEfficiencyUnavailable)
                : ItemBuilder.from(Material.GOLDEN_PICKAXE)
                .name(MessageUtils.format(minEfficiencyName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .lore(MessageUtils.format(minEfficiencyLore).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem(event -> {
                    EfficiencyMineRequirement req = new EfficiencyMineRequirement( configManager,0);
                    req.setEnabled(true);
                    mine.addMineRequirement(req);
                    Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineRequirementsGUI().open(player, mine));
                }));

        gui.setItem(2, 5, hasPermission
                ? unavailable(permissionUnavailable)
                : ItemBuilder.from(Material.WRITABLE_BOOK)
                .name(MessageUtils.format(permissionName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .lore(MessageUtils.format(permissionLore).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem(event -> {
                    PermissionMineRequirement req = new PermissionMineRequirement(configManager, "");
                    req.setEnabled(false);
                    mine.addMineRequirement(req);
                    Bukkit.getScheduler().runTask(plugin, () -> guiManager.getPermissionRequirementGUI().open(player, mine));
                }));

        gui.open(player);
    }

    private GuiItem unavailable(ConfigData<String> name) {
        return ItemBuilder.from(Material.BARRIER)
                .name(MessageUtils.format(name).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem();
    }
}
