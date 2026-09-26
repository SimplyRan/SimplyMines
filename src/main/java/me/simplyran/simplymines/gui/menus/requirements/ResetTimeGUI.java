package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Menu for adjusting a mine's timed reset interval via +/- buttons.
 */
public class ResetTimeGUI extends Menu {

    private static final String PATH = "menus.requirements.reset-time.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Change Reset Time");
    private final ConfigData<String> adjustRemove = lang(PATH + "adjust-remove", "Remove <amount> seconds from Reset Time");
    private final ConfigData<String> adjustAdd = lang(PATH + "adjust-add", "Add <amount> seconds to Reset Time");
    private final ConfigData<String> displayName = lang(PATH + "display-name", "<#ffd166>Reset Time");
    private final ConfigData<String> displayLore = lang(PATH + "display-lore", "<white><seconds>s");

    public ResetTimeGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        TimeResetRequirement req = getOrCreate(mine);

        Gui gui = createGui(4, MessageUtils.format(title),
                () -> services.guiManager().getResetRequirementsGUI().open(player, mine),
                () -> services.mineManager().saveMineAsync(mine));

        renderDisplay(gui, req);
        adjusters(gui, 3, new int[]{1, 5, 10}, adjustRemove, adjustAdd, delta -> adjust(gui, req, delta),
                typeValueButton(player, () -> open(player, mine),
                        value -> adjust(gui, req, (int) Math.round(value) - req.getResetTime())));

        gui.open(player);
    }

    private TimeResetRequirement getOrCreate(BasicMine mine) {
        TimeResetRequirement req = mine.getResetRequirement(TimeResetRequirement.class);
        if (req == null) {
            req = new TimeResetRequirement(30);
            mine.addResetRequirement(req);
        }
        return req;
    }

    private void adjust(Gui gui, TimeResetRequirement req, int delta) {
        req.setResetTime(Math.max(1, req.getResetTime() + delta));
        renderDisplay(gui, req);
        gui.update();
    }

    private void renderDisplay(Gui gui, TimeResetRequirement req) {
        gui.setItem(2, 5, Btn.of(Material.CLOCK, displayName)
                .lore(displayLore, "seconds", String.valueOf(req.getResetTime()))
                .build());
    }
}
