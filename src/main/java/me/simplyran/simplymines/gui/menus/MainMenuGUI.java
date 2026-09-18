package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainMenuGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.main-menu.title", "Select Mine");

    private final ConfigData<String> enabledTrue = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.enabled-true", "<aqua>Mine Enabled: <green>Yes");
    private final ConfigData<String> enabledFalse = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.enabled-false", "<aqua>Mine Enabled: <red>No");
    private final ConfigData<String> resetTime = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.reset-time", "<aqua>Reset Time: <white><value>");
    private final ConfigData<String> warnGlobalTrue = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.warn-global-true", "<aqua>Warn Global: <green>Yes");
    private final ConfigData<String> warnGlobalFalse = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.warn-global-false", "<aqua>Warn Global: <red>No");
    private final ConfigData<String> warnNearTrue = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.warn-near-true", "<aqua>Warn Near: <green>Yes");
    private final ConfigData<String> warnNearFalse = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.warn-near-false", "<aqua>Warn Near: <red>No");
    private final ConfigData<String> warnDistance = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.warn-distance", "<aqua>Warn Distance: <white><value>");
    private final ConfigData<String> teleportPlayersTrue = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.teleport-players-true", "<aqua>Teleport Players: <green>Yes");
    private final ConfigData<String> teleportPlayersFalse = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.teleport-players-false", "<aqua>Teleport Players: <red>No");
    private final ConfigData<String> resetPercentageEnabled = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.reset-percentage-enabled", "<aqua>Reset At Percentage: <green><percent>% left (Enabled)");
    private final ConfigData<String> resetPercentageDisabled = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.reset-percentage-disabled", "<aqua>Reset At Percentage: <red>Disabled");
    private final ConfigData<String> minEfficiencyEnabled = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.min-efficiency-enabled", "<aqua>Min Efficiency: <green>Level <level> (Enabled)");
    private final ConfigData<String> minEfficiencyDisabled = ConfigFactory.newConfigData(
            "menus.main-menu.mine-item.lore.min-efficiency-disabled", "<aqua>Min Efficiency: <red>Disabled");

    private final MineManager mineManager;
    private final GuiManager guiManager;

    public MainMenuGUI(ConfigManager configManager, MineManager mineManager, GuiManager guiManager) {
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(enabledTrue);
        configManager.registerLang(enabledFalse);
        configManager.registerLang(resetTime);
        configManager.registerLang(warnGlobalTrue);
        configManager.registerLang(warnGlobalFalse);
        configManager.registerLang(warnNearTrue);
        configManager.registerLang(warnNearFalse);
        configManager.registerLang(warnDistance);
        configManager.registerLang(teleportPlayersTrue);
        configManager.registerLang(teleportPlayersFalse);
        configManager.registerLang(resetPercentageEnabled);
        configManager.registerLang(resetPercentageDisabled);
        configManager.registerLang(minEfficiencyEnabled);
        configManager.registerLang(minEfficiencyDisabled);
    }

    public void open(Player player) {
        PaginatedGui mainGUI = Gui.paginated()
                .title(MessageUtils.format(title))
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .create();

        GuiUtils.fillRow(mainGUI, 6, Material.WHITE_STAINED_GLASS_PANE);

        mainGUI.setItem(6, 3,
                ItemBuilder.from(Material.ARROW).name(MessageUtils.format(MenuCommonText.PREVIOUS)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> mainGUI.previous()));

        mainGUI.setItem(6, 7,
                ItemBuilder.from(Material.ARROW).name(MessageUtils.format(MenuCommonText.NEXT)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> mainGUI.next()));

        mineManager.getMines().forEach(mine -> mainGUI.addItem(buildMineItem(player, mine)));

        mainGUI.open(player);
    }

    private GuiItem buildMineItem(Player player, BasicMine mine) {
        String mineName = mine.getName();
        List<Component> lore = new ArrayList<>();

        lore.add(MessageUtils.format(mine.isEnabled() ? enabledTrue : enabledFalse)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        lore.add(MessageUtils.format(resetTime, "value", resetTimeLabel(mine))
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        lore.add(MessageUtils.format(mine.isWarnGlobal() ? warnGlobalTrue : warnGlobalFalse)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        lore.add(MessageUtils.format(mine.isWarnNear() ? warnNearTrue : warnNearFalse)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        lore.add(MessageUtils.format(warnDistance, "value", String.valueOf(mine.getWarnDistance()))
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        lore.add(MessageUtils.format(mine.isTeleportPlayers() ? teleportPlayersTrue : teleportPlayersFalse)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        PercentResetRequirement percentRequirement = mine.getResetRequirement(PercentResetRequirement.class);
        if (percentEnabled(mine)) {
            lore.add(MessageUtils.format(resetPercentageEnabled, "percent", String.valueOf(percentRequirement.getResetAtPercentage()))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        } else {
            lore.add(MessageUtils.format(resetPercentageDisabled)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        EfficiencyMineRequirement efficiencyRequirement = mine.getMineRequirement(EfficiencyMineRequirement.class);
        if (minEfficiencyEnabled(mine)) {
            lore.add(MessageUtils.format(minEfficiencyEnabled, "level", String.valueOf(efficiencyRequirement.getEfficiencyLevel()))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        } else {
            lore.add(MessageUtils.format(minEfficiencyDisabled)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        lore.add(MessageUtils.format(MenuCommonText.MATERIALS_HEADER)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            lore.add(MessageUtils.format(MenuCommonText.MATERIAL_LINE,
                            "material", material.getKey(),
                            "percent", String.valueOf(material.getValue() * 100))
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }

        return ItemBuilder.from(ItemUtils.getItemStackFromName(mine.getMainMaterial()))
                .name(Component.text(mineName)
                        .color(NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .lore(lore)
                .asGuiItem(event -> guiManager.getMineEditorGUI().open(player, mineName));
    }

    private boolean minEfficiencyEnabled(BasicMine mine) {
        EfficiencyMineRequirement req = mine.getMineRequirement(EfficiencyMineRequirement.class);
        return req != null && req.isEnabled();
    }

    private String resetTimeLabel(BasicMine mine) {
        TimeResetRequirement req = mine.getResetRequirement(TimeResetRequirement.class);
        return req != null ? req.getResetTime() + "s" : "Not set";
    }

    private boolean percentEnabled(BasicMine mine) {
        PercentResetRequirement req = mine.getResetRequirement(PercentResetRequirement.class);
        return req != null && req.isEnabled();
    }

}
