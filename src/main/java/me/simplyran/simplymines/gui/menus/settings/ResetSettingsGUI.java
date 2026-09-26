package me.simplyran.simplymines.gui.menus.settings;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Hub menu linking to the Timed and Percentage-based reset settings.
 */
public class ResetSettingsGUI extends Menu {

    private final ConfigData<String> title = lang("menus.settings.reset-settings.title", "<dark_gray>Reset Settings");
    private final ConfigData<String> resetTimeName = lang("menus.settings.reset-settings.reset-time-name", "<#ffd166>Reset Time (Timed)");
    private final ConfigData<String> resetTimeLore = lang("menus.settings.reset-settings.reset-time-lore", "<white><value>");
    private final ConfigData<String> resetPercentageName = lang("menus.settings.reset-settings.reset-percentage-name", "<#ffd166>Reset At Percentage");
    private final ConfigData<String> resetPercentageEnabledLore = lang("menus.settings.reset-settings.reset-percentage-enabled-lore", "<#7bd88f><percent>% left (Enabled)");
    private final ConfigData<String> resetPercentageDisabledLore = lang("menus.settings.reset-settings.reset-percentage-disabled-lore", "<#ef6f6c>Disabled");

    public ResetSettingsGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, BasicMine mine) {
        Gui gui = createGui(3, MessageUtils.format(title),
                () -> services.guiManager().getMineEditorGUI().open(player, mine.getName()),
                () -> services.mineManager().saveMineAsync(mine));

        gui.setItem(2, 4, Btn.of(Material.CLOCK, resetTimeName)
                .lore(resetTimeLore(mine))
                .onClick(event -> services.guiManager().getResetTimeGUI().open(player, mine))
                .build());

        gui.setItem(2, 6, Btn.of(Material.REPEATER, resetPercentageName)
                .lore(percentageLore(mine))
                .onClick(event -> services.guiManager().getResetPercentageGUI().open(player, mine))
                .build());

        gui.open(player);
    }

    private Component resetTimeLore(BasicMine mine) {
        TimeResetRequirement req = mine.getResetRequirement(TimeResetRequirement.class);
        return req != null
                ? MessageUtils.format(resetTimeLore, "value", req.getResetTime() + "s")
                : MessageUtils.format(MenuCommonText.NOT_SET).colorIfAbsent(NamedTextColor.WHITE);
    }

    private Component percentageLore(BasicMine mine) {
        PercentResetRequirement req = mine.getResetRequirement(PercentResetRequirement.class);
        return req != null && req.isEnabled()
                ? MessageUtils.format(resetPercentageEnabledLore, "percent", String.valueOf(req.getResetAtPercentage()))
                : MessageUtils.format(resetPercentageDisabledLore);
    }
}
