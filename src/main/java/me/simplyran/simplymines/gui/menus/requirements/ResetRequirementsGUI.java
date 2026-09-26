package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Hub listing every IResetRequirement attached to a mine.
 * Left click a requirement to edit it, shift-right click to remove it,
 * or click "Add Requirement" to attach a new type.
 */
public class ResetRequirementsGUI extends Menu {

    private static final String PATH = "menus.requirements.reset-requirements.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Reset Requirements: <#ffd166><mine>");
    private final ConfigData<String> addRequirement = lang(PATH + "add-requirement", "<#7bd88f>Add Requirement");
    private final ConfigData<String> timeResetName = lang(PATH + "time-reset-name", "<#ffd166>Time Reset");
    private final ConfigData<String> timeResetLore = lang(PATH + "time-reset-lore", "<#8b9bb4>Resets every <white><seconds>s");
    private final ConfigData<String> percentResetEnabled = lang(PATH + "percent-reset-enabled", "<white>Percent Reset: <#7bd88f>Enabled");
    private final ConfigData<String> percentResetDisabled = lang(PATH + "percent-reset-disabled", "<white>Percent Reset: <#ef6f6c>Disabled");
    private final ConfigData<String> percentResetLore = lang(PATH + "percent-reset-lore", "<#8b9bb4>Resets at <white><percent>% left");
    private final ConfigData<String> unknownRequirement = lang(PATH + "unknown-requirement", "<white><type>");

    public ResetRequirementsGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        PaginatedGui gui = createPaginated(2, MessageUtils.format(title, "mine", mine.getName()),
                () -> services.guiManager().getMineEditorGUI().open(player, mine.getName()),
                () -> services.mineManager().saveMineAsync(mine));

        gui.setItem(2, 9, Btn.of(Material.EMERALD, addRequirement)
                .onClick(event -> services.guiManager().getAddResetRequirementGUI().open(player, mine))
                .build());

        for (IResetRequirement requirement : mine.getResetRequirements()) {
            gui.addItem(buildItem(player, mine, requirement));
        }

        gui.open(player);
    }

    private GuiItem buildItem(Player player, BasicMine mine, IResetRequirement requirement) {
        if (requirement instanceof TimeResetRequirement time) {
            return editableItem(Btn.of(Material.CLOCK, timeResetName),
                    List.of(MessageUtils.format(timeResetLore, "seconds", String.valueOf(time.getResetTime()))),
                    () -> services.guiManager().getResetTimeGUI().open(player, mine),
                    () -> remove(player, mine, time));
        }

        if (requirement instanceof PercentResetRequirement percent) {
            return editableItem(Btn.of(Material.REPEATER, percent.isEnabled() ? percentResetEnabled : percentResetDisabled),
                    List.of(MessageUtils.format(percentResetLore, "percent", String.valueOf(percent.getResetAtPercentage()))),
                    () -> services.guiManager().getResetPercentageGUI().open(player, mine),
                    () -> remove(player, mine, percent));
        }

        return Btn.of(Material.PAPER, unknownRequirement, "type", requirement.getClass().getSimpleName()).build();
    }

    private void remove(Player player, BasicMine mine, IResetRequirement requirement) {
        mine.removeResetRequirement(requirement);
        reopenLater(() -> open(player, mine));
    }
}
