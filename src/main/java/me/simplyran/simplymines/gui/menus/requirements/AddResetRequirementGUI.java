package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class AddResetRequirementGUI extends Menu {

    private static final String PATH = "menus.requirements.add-reset-requirement.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Add Reset Requirement");
    private final ConfigData<String> timeResetUnavailable = lang(PATH + "time-reset-unavailable", "<#ef6f6c>Time Reset (Already Added)");
    private final ConfigData<String> timeResetName = lang(PATH + "time-reset-name", "<#ffd166>Time Reset");
    private final ConfigData<String> timeResetLore = lang(PATH + "time-reset-lore", "<#8b9bb4>Resets on a fixed timer");
    private final ConfigData<String> percentResetUnavailable = lang(PATH + "percent-reset-unavailable", "<#ef6f6c>Percent Reset (Already Added)");
    private final ConfigData<String> percentResetName = lang(PATH + "percent-reset-name", "<#ffd166>Percent Reset");
    private final ConfigData<String> percentResetLore = lang(PATH + "percent-reset-lore", "<#8b9bb4>Resets once enough of the mine is broken");

    public AddResetRequirementGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        Runnable back = () -> services.guiManager().getResetRequirementsGUI().open(player, mine);
        Gui gui = createGui(3, MessageUtils.format(title), back, null);

        boolean hasTime = mine.getResetRequirement(TimeResetRequirement.class) != null;
        boolean hasPercent = mine.getResetRequirement(PercentResetRequirement.class) != null;

        gui.setItem(2, 4, hasTime
                ? unavailable(timeResetUnavailable)
                : Btn.of(Material.CLOCK, timeResetName)
                .lore(timeResetLore)
                .onClick(event -> {
                    mine.addResetRequirement(new TimeResetRequirement(30));
                    reopenLater(back);
                })
                .build());

        gui.setItem(2, 6, hasPercent
                ? unavailable(percentResetUnavailable)
                : Btn.of(Material.REPEATER, percentResetName)
                .lore(percentResetLore)
                .onClick(event -> {
                    PercentResetRequirement req = new PercentResetRequirement(mine, 10.0);
                    req.setEnabled(true);
                    mine.addResetRequirement(req);
                    reopenLater(back);
                })
                .build());

        gui.open(player);
    }

    private GuiItem unavailable(ConfigData<String> name) {
        return Btn.of(Material.BARRIER, name).build();
    }
}
