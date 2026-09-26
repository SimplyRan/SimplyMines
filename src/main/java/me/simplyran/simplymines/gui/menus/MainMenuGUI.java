package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.Numbers;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;

public class MainMenuGUI extends Menu {

    private static final String PATH = "menus.main-menu.";
    private static final String LORE = PATH + "mine-item.lore.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Select Mine");

    private final ConfigData<String> enabledTrue = lang(LORE + "enabled-true", "<#8b9bb4>Mine Enabled: <#7bd88f>Yes");
    private final ConfigData<String> enabledFalse = lang(LORE + "enabled-false", "<#8b9bb4>Mine Enabled: <#ef6f6c>No");
    private final ConfigData<String> resetTime = lang(LORE + "reset-time", "<#8b9bb4>Reset Time: <white><value>");
    private final ConfigData<String> warnGlobalTrue = lang(LORE + "warn-global-true", "<#8b9bb4>Warn Global: <#7bd88f>Yes");
    private final ConfigData<String> warnGlobalFalse = lang(LORE + "warn-global-false", "<#8b9bb4>Warn Global: <#ef6f6c>No");
    private final ConfigData<String> warnNearTrue = lang(LORE + "warn-near-true", "<#8b9bb4>Warn Near: <#7bd88f>Yes");
    private final ConfigData<String> warnNearFalse = lang(LORE + "warn-near-false", "<#8b9bb4>Warn Near: <#ef6f6c>No");
    private final ConfigData<String> warnDistance = lang(LORE + "warn-distance", "<#8b9bb4>Warn Distance: <white><value>");
    private final ConfigData<String> teleportPlayersTrue = lang(LORE + "teleport-players-true", "<#8b9bb4>Teleport Players: <#7bd88f>Yes");
    private final ConfigData<String> teleportPlayersFalse = lang(LORE + "teleport-players-false", "<#8b9bb4>Teleport Players: <#ef6f6c>No");
    private final ConfigData<String> resetPercentageEnabled = lang(LORE + "reset-percentage-enabled", "<#8b9bb4>Reset At Percentage: <#7bd88f><percent>% left (Enabled)");
    private final ConfigData<String> resetPercentageDisabled = lang(LORE + "reset-percentage-disabled", "<#8b9bb4>Reset At Percentage: <#ef6f6c>Disabled");

    public MainMenuGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player) {
        PaginatedGui gui = createPaginated(6, MessageUtils.format(title), null, null);

        services.mineManager().getMines().forEach(mine -> gui.addItem(buildMineItem(player, mine)));

        gui.open(player);
    }

    private GuiItem buildMineItem(Player player, BasicMine mine) {
        String mineName = mine.getName();

        Btn btn = Btn.of(ItemUtils.getItemStackFromName(mine.getMainMaterial()),
                        Component.text(mineName).color(NamedTextColor.YELLOW))
                .lore(mine.isEnabled() ? enabledTrue : enabledFalse)
                .lore(resetTime, "value", resetTimeLabel(mine))
                .lore(mine.isWarnGlobal() ? warnGlobalTrue : warnGlobalFalse)
                .lore(mine.isWarnNear() ? warnNearTrue : warnNearFalse)
                .lore(warnDistance, "value", String.valueOf(mine.getWarnDistance()))
                .lore(mine.isTeleportPlayers() ? teleportPlayersTrue : teleportPlayersFalse);

        PercentResetRequirement percent = mine.getResetRequirement(PercentResetRequirement.class);
        if (percent != null && percent.isEnabled()) {
            btn.lore(resetPercentageEnabled, "percent", String.valueOf(percent.getResetAtPercentage()));
        } else {
            btn.lore(resetPercentageDisabled);
        }

        btn.lore(MenuCommonText.MATERIALS_HEADER);
        for (Map.Entry<String, Double> material : mine.getMaterials()) {
            btn.lore(MenuCommonText.MATERIAL_LINE,
                    "material", material.getKey(),
                    "percent", Numbers.percent(material.getValue()));
        }

        return btn.onClick(event -> services.guiManager().getMineEditorGUI().open(player, mineName)).build();
    }

    private String resetTimeLabel(BasicMine mine) {
        TimeResetRequirement req = mine.getResetRequirement(TimeResetRequirement.class);
        return req != null ? req.getResetTime() + "s" : "Not set";
    }
}
