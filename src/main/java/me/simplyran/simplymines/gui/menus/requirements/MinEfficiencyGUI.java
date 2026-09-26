package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Menu for adjusting/toggling a mine's minimum required tool-efficiency level.
 */
public class MinEfficiencyGUI extends Menu {

    private static final String PATH = "menus.requirements.min-efficiency.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Min Efficiency");
    private final ConfigData<String> adjustRemoveLevels = lang(PATH + "adjust-remove-levels", "Remove <amount> Level");
    private final ConfigData<String> adjustAddLevels = lang(PATH + "adjust-add-levels", "Add <amount> Level");
    private final ConfigData<String> displayName = lang(PATH + "display-name", "<#ffd166>Required Efficiency Level");
    private final ConfigData<String> displayLore = lang(PATH + "display-lore", "<white>Level <level>");
    private final ConfigData<String> toggleLabel = lang(PATH + "toggle-label", "Min Efficiency");

    public MinEfficiencyGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        EfficiencyMineRequirement req = getOrCreate(mine);

        Gui gui = createGui(5, MessageUtils.format(title),
                () -> services.guiManager().getMineRequirementsGUI().open(player, mine),
                () -> services.mineManager().saveMineAsync(mine));

        renderDisplay(gui, req);
        adjusters(gui, 3, new int[]{1, 5}, adjustRemoveLevels, adjustAddLevels, delta -> adjust(gui, req, delta),
                typeValueButton(player, () -> open(player, mine),
                        value -> adjust(gui, req, (int) Math.round(value) - req.getEfficiencyLevel())));
        new ToggleButton(gui, 4, 5, MessageUtils.format(toggleLabel), req::isEnabled, req::setEnabled, null).render();

        gui.open(player);
    }

    private EfficiencyMineRequirement getOrCreate(BasicMine mine) {
        EfficiencyMineRequirement req = mine.getMineRequirement(EfficiencyMineRequirement.class);
        if (req == null) {
            req = new EfficiencyMineRequirement(services.configManager(), 0);
            req.setEnabled(false);
            mine.addMineRequirement(req);
        }
        return req;
    }

    private void adjust(Gui gui, EfficiencyMineRequirement req, int delta) {
        req.setEfficiencyLevel(Math.max(0, req.getEfficiencyLevel() + delta));
        renderDisplay(gui, req);
        gui.update();
    }

    private void renderDisplay(Gui gui, EfficiencyMineRequirement req) {
        gui.setItem(2, 5, Btn.of(Material.GOLDEN_PICKAXE, displayName)
                .lore(displayLore, "level", String.valueOf(req.getEfficiencyLevel()))
                .build());
    }
}
