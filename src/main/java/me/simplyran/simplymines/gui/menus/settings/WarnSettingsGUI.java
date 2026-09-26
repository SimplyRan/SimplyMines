package me.simplyran.simplymines.gui.menus.settings;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Hub menu linking to Warn Seconds and Warn Distance settings.
 */
public class WarnSettingsGUI extends Menu {

    private static final String PATH = "menus.settings.warn-settings.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Warn Settings");
    private final ConfigData<String> warnSecondsName = lang(PATH + "warn-seconds-name", "<#ffd166>Warn Seconds");
    private final ConfigData<String> warnSecondsLore = lang(PATH + "warn-seconds-lore", "<#8b9bb4>Choose when reset warnings are sent");
    private final ConfigData<String> warnDistanceName = lang(PATH + "warn-distance-name", "<#ffd166>Warn Distance");
    private final ConfigData<String> warnDistanceLore = lang(PATH + "warn-distance-lore", "<#8b9bb4>Current: <white><amount> blocks");

    public WarnSettingsGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = createGui(3, MessageUtils.format(title),
                () -> services.guiManager().getMineEditorGUI().open(player, mine.getName()),
                () -> services.mineManager().saveMineAsync(mine));

        gui.setItem(2, 4, Btn.of(Material.BELL, warnSecondsName)
                .lore(warnSecondsLore)
                .onClick(event -> services.guiManager().getWarnSecondsGUI().open(player, mine))
                .build());

        gui.setItem(2, 6, Btn.of(Material.SPYGLASS, warnDistanceName)
                .lore(warnDistanceLore, "amount", String.valueOf(mine.getWarnDistance()))
                .onClick(event -> services.guiManager().getWarnDistanceGUI().open(player, mine))
                .build());

        gui.open(player);
    }
}
