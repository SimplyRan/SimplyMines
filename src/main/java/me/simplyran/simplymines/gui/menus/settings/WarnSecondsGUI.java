package me.simplyran.simplymines.gui.menus.settings;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Menu for toggling which warn-second thresholds are active for a mine.
 */
public class WarnSecondsGUI extends Menu {

    private static final String PATH = "menus.settings.warn-seconds.";
    private static final int[] WARN_SECOND_OPTIONS = {1, 2, 5, 10, 15, 30, 60};
    // Slot per option: first four on row 3, remaining three on row 4.
    private static final int[][] SLOTS = {{3, 3}, {3, 4}, {3, 6}, {3, 7}, {4, 4}, {4, 5}, {4, 6}};

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Warn Seconds");
    private final ConfigData<String> toggleLabel = lang(PATH + "toggle-label", "<seconds> Warn Seconds");
    private final ConfigData<String> summaryName = lang(PATH + "summary-name", "<#ffd166>Warn Times");
    private final ConfigData<String> summaryLore = lang(PATH + "summary-lore", "<#8b9bb4>Warns at: <white><seconds>");

    public WarnSecondsGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = createGui(5, MessageUtils.format(title),
                () -> services.guiManager().getWarnSettingsGUI().open(player, mine),
                () -> services.mineManager().saveMineAsync(mine));

        List<Integer> warnSec = mine.getWarnSeconds();
        renderSummary(gui, warnSec);

        for (int i = 0; i < WARN_SECOND_OPTIONS.length; i++) {
            int seconds = WARN_SECOND_OPTIONS[i];
            new ToggleButton(gui, SLOTS[i][0], SLOTS[i][1],
                    MessageUtils.format(toggleLabel, "seconds", String.valueOf(seconds)),
                    () -> warnSec.contains(seconds),
                    enabled -> {
                        if (enabled) {
                            if (!warnSec.contains(seconds)) warnSec.add(seconds);
                        } else {
                            warnSec.remove(Integer.valueOf(seconds));
                        }
                    },
                    () -> renderSummary(gui, warnSec)
            ).render();
        }

        gui.open(player);
    }

    private void renderSummary(Gui gui, List<Integer> warnSec) {
        String seconds = warnSec.isEmpty()
                ? MessageUtils.plainFormat(MenuCommonText.NOT_SET)
                : warnSec.stream().sorted().map(s -> s + "s").collect(Collectors.joining(", "));

        gui.setItem(2, 5, Btn.of(Material.BELL, summaryName)
                .lore(summaryLore, "seconds", seconds)
                .build());
        gui.update();
    }
}
