package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Menu for adjusting/toggling a mine's reset-at-percentage threshold.
 */
public class ResetPercentageGUI extends Menu {

    private static final String PATH = "menus.requirements.reset-percentage.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Reset At Percentage");
    private final ConfigData<String> adjustRemove = lang(PATH + "adjust-remove", "Remove <amount>% from Reset Threshold");
    private final ConfigData<String> adjustAdd = lang(PATH + "adjust-add", "Add <amount>% to Reset Threshold");
    private final ConfigData<String> displayName = lang(PATH + "display-name", "<#ffd166>Reset Threshold");
    private final ConfigData<String> displayLore = lang(PATH + "display-lore", "<white><percent>% left");
    private final ConfigData<String> toggleLabel = lang(PATH + "toggle-label", "Reset At Percentage");

    public ResetPercentageGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        PercentResetRequirement req = getOrCreate(mine);

        Gui gui = createGui(5, MessageUtils.format(title),
                () -> services.guiManager().getResetRequirementsGUI().open(player, mine),
                () -> services.mineManager().saveMineAsync(mine));

        renderDisplay(gui, req);
        adjusters(gui, 3, new int[]{1, 5, 10}, adjustRemove, adjustAdd, delta -> adjust(gui, req, delta),
                typeValueButton(player, () -> open(player, mine),
                        value -> adjust(gui, req, value - req.getResetAtPercentage())));
        new ToggleButton(gui, 4, 5, MessageUtils.format(toggleLabel), req::isEnabled, req::setEnabled, null).render();

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
        req.setResetAtPercentage(Math.clamp(req.getResetAtPercentage() + delta, 0, 100));
        renderDisplay(gui, req);
        gui.update();
    }

    private void renderDisplay(Gui gui, PercentResetRequirement req) {
        gui.setItem(2, 5, Btn.of(Material.COMPARATOR, displayName)
                .lore(displayLore, "percent", String.valueOf(req.getResetAtPercentage()))
                .build());
    }
}
