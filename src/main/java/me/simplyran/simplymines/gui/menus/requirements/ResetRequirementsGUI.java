package me.simplyran.simplymines.gui.menus.requirements;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

/**
 * Hub listing every IResetRequirement attached to a mine.
 * Left click a requirement to edit it, shift-right click to remove it,
 * or click "Add Requirement" to attach a new type.
 */
public class ResetRequirementsGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.requirements.reset-requirements.title", "Reset Requirements: <mine>");
    private final ConfigData<String> addRequirement = ConfigFactory.newConfigData(
            "menus.requirements.reset-requirements.add-requirement", "<green>Add Requirement");
    private final ConfigData<String> timeResetName = ConfigFactory.newConfigData(
            "menus.requirements.reset-requirements.time-reset-name", "<yellow>Time Reset");
    private final ConfigData<String> timeResetLore = ConfigFactory.newConfigData(
            "menus.requirements.reset-requirements.time-reset-lore", "<gray>Resets every <white><seconds>s");
    private final ConfigData<String> percentResetEnabled = ConfigFactory.newConfigData(
            "menus.requirements.reset-requirements.percent-reset-enabled", "<white>Percent Reset: <green>Enabled");
    private final ConfigData<String> percentResetDisabled = ConfigFactory.newConfigData(
            "menus.requirements.reset-requirements.percent-reset-disabled", "<white>Percent Reset: <red>Disabled");
    private final ConfigData<String> percentResetLore = ConfigFactory.newConfigData(
            "menus.requirements.reset-requirements.percent-reset-lore", "<gray>Resets at <white><percent>% left");
    private final ConfigData<String> unknownRequirement = ConfigFactory.newConfigData(
            "menus.requirements.reset-requirements.unknown-requirement", "<white><type>");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public ResetRequirementsGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(addRequirement);
        configManager.registerLang(timeResetName);
        configManager.registerLang(timeResetLore);
        configManager.registerLang(percentResetEnabled);
        configManager.registerLang(percentResetDisabled);
        configManager.registerLang(percentResetLore);
        configManager.registerLang(unknownRequirement);
    }

    public void open(Player player, BasicMine mine) {
        PaginatedGui gui = Gui.paginated()
                .title(MessageUtils.format(title, "mine", mine.getName()))
                .rows(2)
                .pageSize(9)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;

            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineEditorGUI().open(player, mine.getName()));
            mineManager.saveMineAsync(mine);
        });

        GuiUtils.fillRow(gui, 2, Material.WHITE_STAINED_GLASS_PANE);

        gui.setItem(2, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> {
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineEditorGUI().open(player, mine.getName()));
                            mineManager.saveMineAsync(mine);
                        }));

        gui.setItem(2, 3,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.PREVIOUS).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.previous()));

        gui.setItem(2, 7,
                ItemBuilder.from(Material.ARROW)
                        .name(MessageUtils.format(MenuCommonText.NEXT).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.next()));

        gui.setItem(2, 9,
                ItemBuilder.from(Material.EMERALD)
                        .name(MessageUtils.format(addRequirement).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> guiManager.getAddResetRequirementGUI().open(player, mine)));

        for (IResetRequirement requirement : mine.getResetRequirements()) {
            gui.addItem(buildItem(player, mine, requirement));
        }

        gui.open(player);
    }

    private GuiItem buildItem(Player player, BasicMine mine, IResetRequirement requirement) {

        if (requirement instanceof TimeResetRequirement time) {
            return ItemBuilder.from(Material.CLOCK)
                    .name(MessageUtils.format(timeResetName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .lore(List.of(
                            MessageUtils.format(timeResetLore, "seconds", String.valueOf(time.getResetTime()))
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            Component.empty(),
                            MessageUtils.format(MenuCommonText.LEFT_CLICK_EDIT).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            MessageUtils.format(MenuCommonText.SHIFT_RIGHT_CLICK_REMOVE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    ))
                    .asGuiItem(event -> {
                        if (event.getClick() == ClickType.SHIFT_RIGHT) {
                            mine.removeResetRequirement(time);
                            Bukkit.getScheduler().runTask(plugin, () -> open(player, mine));
                            return;
                        }
                        guiManager.getResetTimeGUI().open(player, mine);
                    });
        }

        if (requirement instanceof PercentResetRequirement percent) {
            boolean enabled = percent.isEnabled();
            return ItemBuilder.from(Material.REPEATER)
                    .name(MessageUtils.format(enabled ? percentResetEnabled : percentResetDisabled).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .lore(List.of(
                            MessageUtils.format(percentResetLore, "percent", String.valueOf(percent.getResetAtPercentage()))
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            Component.empty(),
                            MessageUtils.format(MenuCommonText.LEFT_CLICK_EDIT).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            MessageUtils.format(MenuCommonText.SHIFT_RIGHT_CLICK_REMOVE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    ))
                    .asGuiItem(event -> {
                        if (event.getClick() == ClickType.SHIFT_RIGHT) {
                            mine.removeResetRequirement(percent);
                            Bukkit.getScheduler().runTask(plugin, () -> open(player, mine));
                            return;
                        }
                        guiManager.getResetPercentageGUI().open(player, mine);
                    });
        }

        return ItemBuilder.from(Material.PAPER)
                .name(MessageUtils.format(unknownRequirement, "type", requirement.getClass().getSimpleName())
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem();
    }
}
