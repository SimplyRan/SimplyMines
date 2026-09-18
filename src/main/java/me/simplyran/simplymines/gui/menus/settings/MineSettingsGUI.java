package me.simplyran.simplymines.gui.menus.settings;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
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
 * All of a mine's on/off settings in one place, grouped by topic,
 * so the editor hub stays a clean navigation menu.
 */
public class MineSettingsGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.title", "Settings: <mine>");

    private final ConfigData<String> generalSectionLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.general-section-label", "<gold>General");
    private final ConfigData<String> miningSectionLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.mining-section-label", "<gold>Mining");
    private final ConfigData<String> resetWarningsSectionLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.reset-warnings-section-label", "<gold>Reset & Warnings");

    private final ConfigData<String> mineEnabledLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.mine-enabled-label", "Mine Enabled");
    private final ConfigData<String> mineEnabledDescription = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.mine-enabled-description", "Master switch - disabled mines never reset.");
    private final ConfigData<String> teleportPlayersLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.teleport-players-label", "Teleport Players");
    private final ConfigData<String> teleportPlayersDescription = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.teleport-players-description", "Teleport players out of the mine before a reset.");

    private final ConfigData<String> normalDropsLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.normal-drops-label", "Normal Drops");
    private final ConfigData<String> normalDropsDescription = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.normal-drops-description", "Broken blocks drop their vanilla items.");
    private final ConfigData<String> autoPickupLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.auto-pickup-label", "Auto Pickup");
    private final ConfigData<String> autoPickupDescription = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.auto-pickup-description", "Drops go straight into the player's inventory.");
    private final ConfigData<String> fortuneLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.fortune-label", "Fortune");
    private final ConfigData<String> fortuneDescription = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.fortune-description", "Fortune on the tool multiplies item drops.");

    private final ConfigData<String> replaceModeLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.replace-mode-label", "Replace Mode");
    private final ConfigData<String> replaceModeDescription = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.replace-mode-description", "Reset overwrites every block, not just air.");
    private final ConfigData<String> usePhysicsLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.use-physics-label", "Use Physics");
    private final ConfigData<String> usePhysicsDescription = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.use-physics-description", "Placed blocks trigger physics updates.");
    private final ConfigData<String> warnGlobalLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.warn-global-label", "Warn Global");
    private final ConfigData<String> warnGlobalDescription = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.warn-global-description", "Broadcast reset warnings to the whole server.");
    private final ConfigData<String> warnNearLabel = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.warn-near-label", "Warn Near");
    private final ConfigData<String> warnNearDescription = ConfigFactory.newConfigData(
            "menus.settings.mine-settings.warn-near-description", "Warn only players near the mine before a reset.");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public MineSettingsGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(generalSectionLabel);
        configManager.registerLang(miningSectionLabel);
        configManager.registerLang(resetWarningsSectionLabel);
        configManager.registerLang(mineEnabledLabel);
        configManager.registerLang(mineEnabledDescription);
        configManager.registerLang(teleportPlayersLabel);
        configManager.registerLang(teleportPlayersDescription);
        configManager.registerLang(normalDropsLabel);
        configManager.registerLang(normalDropsDescription);
        configManager.registerLang(autoPickupLabel);
        configManager.registerLang(autoPickupDescription);
        configManager.registerLang(fortuneLabel);
        configManager.registerLang(fortuneDescription);
        configManager.registerLang(replaceModeLabel);
        configManager.registerLang(replaceModeDescription);
        configManager.registerLang(usePhysicsLabel);
        configManager.registerLang(usePhysicsDescription);
        configManager.registerLang(warnGlobalLabel);
        configManager.registerLang(warnGlobalDescription);
        configManager.registerLang(warnNearLabel);
        configManager.registerLang(warnNearDescription);
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = Gui.gui()
                .rows(5)
                .title(MessageUtils.format(title, "mine", mine.getName()))
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
                        .name(MessageUtils.format(MenuCommonText.BACK)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        sectionLabel(gui, 2, 2, Material.NETHER_STAR, generalSectionLabel);
        new ToggleButton(gui, 2, 4, MessageUtils.plainFormat(mineEnabledLabel),
                MessageUtils.plainFormat(mineEnabledDescription),
                mine::isEnabled, mine::setEnabled, save(mine)).render();
        new ToggleButton(gui, 2, 6, MessageUtils.plainFormat(teleportPlayersLabel),
                MessageUtils.plainFormat(teleportPlayersDescription),
                mine::isTeleportPlayers, mine::setTeleportPlayers, save(mine)).render();

        sectionLabel(gui, 3, 2, Material.IRON_PICKAXE, miningSectionLabel);
        new ToggleButton(gui, 3, 4, MessageUtils.plainFormat(normalDropsLabel),
                MessageUtils.plainFormat(normalDropsDescription),
                mine::isNormalDropsEnabled, mine::setNormalDropsEnabled, save(mine)).render();
        new ToggleButton(gui, 3, 6, MessageUtils.plainFormat(autoPickupLabel),
                MessageUtils.plainFormat(autoPickupDescription),
                mine::isAutoPickup, mine::setAutoPickup, save(mine)).render();
        new ToggleButton(gui, 3, 8, MessageUtils.plainFormat(fortuneLabel),
                MessageUtils.plainFormat(fortuneDescription),
                mine::isFortuneEnabled, mine::setFortuneEnabled, save(mine)).render();

        sectionLabel(gui, 4, 2, Material.TNT, resetWarningsSectionLabel);
        new ToggleButton(gui, 4, 4, MessageUtils.plainFormat(replaceModeLabel),
                MessageUtils.plainFormat(replaceModeDescription),
                mine::isReplaceMode, mine::setReplaceMode, save(mine)).render();
        new ToggleButton(gui, 4, 5, MessageUtils.plainFormat(usePhysicsLabel),
                MessageUtils.plainFormat(usePhysicsDescription),
                mine::isUsePhysics, mine::setUsePhysics, save(mine)).render();
        new ToggleButton(gui, 4, 7, MessageUtils.plainFormat(warnGlobalLabel),
                MessageUtils.plainFormat(warnGlobalDescription),
                mine::isWarnGlobal, mine::setWarnGlobal, save(mine)).render();
        new ToggleButton(gui, 4, 8, MessageUtils.plainFormat(warnNearLabel),
                MessageUtils.plainFormat(warnNearDescription),
                mine::isWarnNear, mine::setWarnNear, save(mine)).render();

        gui.open(player);
    }

    private Runnable save(BasicMine mine) {
        return () -> mineManager.saveMineAsync(mine);
    }

    private void sectionLabel(Gui gui, int row, int col, Material icon, ConfigData<String> label) {
        gui.setItem(row, col,
                ItemBuilder.from(icon)
                        .name(MessageUtils.format(label)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .decorate(TextDecoration.BOLD))
                        .asGuiItem());
    }
}
