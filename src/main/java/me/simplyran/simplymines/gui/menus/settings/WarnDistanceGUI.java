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
 * Menu for adjusting a mine's warn distance via +/- buttons.
 */
public class WarnDistanceGUI extends Menu {

    private static final String PATH = "menus.settings.warn-distance.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Change Warn Distance");
    private final ConfigData<String> warnDistanceName = lang(PATH + "warn-distance-name", "<#ffd166>Warn Distance");
    private final ConfigData<String> warnDistanceLore = lang(PATH + "warn-distance-lore", "<white><amount> Blocks");
    private final ConfigData<String> removeLabel = lang(PATH + "remove-label", "Remove <amount> from Warn Distance");
    private final ConfigData<String> addLabel = lang(PATH + "add-label", "Add <amount> to Warn Distance");

    public WarnDistanceGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = createGui(4, MessageUtils.format(title),
                () -> services.guiManager().getWarnSettingsGUI().open(player, mine),
                () -> services.mineManager().saveMineAsync(mine));

        renderDisplay(gui, mine);
        adjusters(gui, 3, new int[]{1, 5, 10}, removeLabel, addLabel, delta -> adjust(gui, mine, delta),
                typeValueButton(player, () -> open(player, mine),
                        value -> adjust(gui, mine, (int) Math.round(value) - mine.getWarnDistance())));

        gui.open(player);
    }

    private void adjust(Gui gui, BasicMine mine, int delta) {
        mine.setWarnDistance(Math.max(0, mine.getWarnDistance() + delta));
        renderDisplay(gui, mine);
        gui.update();
    }

    private void renderDisplay(Gui gui, BasicMine mine) {
        gui.setItem(2, 5, Btn.of(Material.COMPASS, warnDistanceName)
                .lore(warnDistanceLore, "amount", String.valueOf(mine.getWarnDistance()))
                .build());
    }
}
