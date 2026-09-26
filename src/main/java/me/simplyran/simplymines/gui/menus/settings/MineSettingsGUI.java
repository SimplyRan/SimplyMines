package me.simplyran.simplymines.gui.menus.settings;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * All of a mine's on/off settings in one place, grouped by topic,
 * so the editor hub stays a clean navigation menu.
 */
public class MineSettingsGUI extends Menu {

    private static final String PATH = "menus.settings.mine-settings.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Settings: <#ffd166><mine>");

    private final ConfigData<String> generalSectionLabel = lang(PATH + "general-section-label", "<#ffd166>General");
    private final ConfigData<String> miningSectionLabel = lang(PATH + "mining-section-label", "<#ffd166>Mining");
    private final ConfigData<String> resetSectionLabel = lang(PATH + "reset-warnings-section-label", "<#ffd166>Reset");
    private final ConfigData<String> warningsSectionLabel = lang(PATH + "warnings-section-label", "<#ffd166>Warnings");

    private final ConfigData<String> mineEnabledLabel = lang(PATH + "mine-enabled-label", "Mine Enabled");
    private final ConfigData<String> mineEnabledDescription = lang(PATH + "mine-enabled-description", "Master switch - disabled mines never reset.");
    private final ConfigData<String> teleportPlayersLabel = lang(PATH + "teleport-players-label", "Teleport Players");
    private final ConfigData<String> teleportPlayersDescription = lang(PATH + "teleport-players-description", "Teleport players out of the mine before a reset.");

    private final ConfigData<String> normalDropsLabel = lang(PATH + "normal-drops-label", "Normal Drops");
    private final ConfigData<String> normalDropsDescription = lang(PATH + "normal-drops-description", "Broken blocks drop their vanilla items.");
    private final ConfigData<String> autoPickupLabel = lang(PATH + "auto-pickup-label", "Auto Pickup");
    private final ConfigData<String> autoPickupDescription = lang(PATH + "auto-pickup-description", "Drops go straight into the player's inventory.");
    private final ConfigData<String> autoSmeltLabel = lang(PATH + "auto-smelt-label", "Auto Smelt");
    private final ConfigData<String> autoSmeltDescription = lang(PATH + "auto-smelt-description", "Drops are smelted like in a furnace.");
    private final ConfigData<String> fortuneLabel = lang(PATH + "fortune-label", "Fortune");
    private final ConfigData<String> fortuneDescription = lang(PATH + "fortune-description", "Fortune on the tool multiplies item drops.");

    private final ConfigData<String> replaceModeLabel = lang(PATH + "replace-mode-label", "Replace Mode");
    private final ConfigData<String> replaceModeDescription = lang(PATH + "replace-mode-description", "Reset overwrites every block, not just air.");
    private final ConfigData<String> usePhysicsLabel = lang(PATH + "use-physics-label", "Use Physics");
    private final ConfigData<String> usePhysicsDescription = lang(PATH + "use-physics-description", "Placed blocks trigger physics updates.");
    private final ConfigData<String> warnGlobalLabel = lang(PATH + "warn-global-label", "Warn Global");
    private final ConfigData<String> warnGlobalDescription = lang(PATH + "warn-global-description", "Broadcast reset warnings to the whole server.");
    private final ConfigData<String> warnNearLabel = lang(PATH + "warn-near-label", "Warn Near");
    private final ConfigData<String> warnNearDescription = lang(PATH + "warn-near-description", "Warn only players near the mine before a reset.");

    public MineSettingsGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = createGui(6, MessageUtils.format(title, "mine", mine.getName()),
                () -> services.guiManager().getMineEditorGUI().open(player, mine.getName()),
                () -> services.mineManager().saveMineAsync(mine));

        sectionLabel(gui, 2, Material.NETHER_STAR, generalSectionLabel);
        toggle(gui, mine, 2, 4, mineEnabledLabel, mineEnabledDescription, mine::isEnabled, mine::setEnabled);
        toggle(gui, mine, 2, 5, teleportPlayersLabel, teleportPlayersDescription, mine::isTeleportPlayers, mine::setTeleportPlayers);

        sectionLabel(gui, 3, Material.IRON_PICKAXE, miningSectionLabel);
        toggle(gui, mine, 3, 4, normalDropsLabel, normalDropsDescription, mine::isNormalDropsEnabled, mine::setNormalDropsEnabled);
        toggle(gui, mine, 3, 5, autoPickupLabel, autoPickupDescription, mine::isAutoPickup, mine::setAutoPickup);
        toggle(gui, mine, 3, 6, autoSmeltLabel, autoSmeltDescription, mine::isAutoSmelt, mine::setAutoSmelt);
        toggle(gui, mine, 3, 7, fortuneLabel, fortuneDescription, mine::isFortuneEnabled, mine::setFortuneEnabled);

        sectionLabel(gui, 4, Material.TNT, resetSectionLabel);
        toggle(gui, mine, 4, 4, replaceModeLabel, replaceModeDescription, mine::isReplaceMode, mine::setReplaceMode);
        toggle(gui, mine, 4, 5, usePhysicsLabel, usePhysicsDescription, mine::isUsePhysics, mine::setUsePhysics);

        sectionLabel(gui, 5, Material.BELL, warningsSectionLabel);
        toggle(gui, mine, 5, 4, warnGlobalLabel, warnGlobalDescription, mine::isWarnGlobal, mine::setWarnGlobal);
        toggle(gui, mine, 5, 5, warnNearLabel, warnNearDescription, mine::isWarnNear, mine::setWarnNear);

        gui.open(player);
    }

    private void toggle(Gui gui, BasicMine mine, int row, int col, ConfigData<String> label, ConfigData<String> description,
                        BooleanSupplier getter, Consumer<Boolean> setter) {
        new ToggleButton(gui, row, col, MessageUtils.format(label), MessageUtils.format(description),
                getter, setter, () -> services.mineManager().saveMineAsync(mine)).render();
    }

    private void sectionLabel(Gui gui, int row, Material icon, ConfigData<String> label) {
        gui.setItem(row, 2, Btn.of(icon, MessageUtils.format(label)).build());
    }
}
