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
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
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
 * Hub listing every IMineRequirement attached to a mine (things a player
 * must satisfy to mine here, e.g. tool efficiency or permission).
 */
public class MineRequirementsGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.requirements.mine-requirements.title", "Mine Requirements: <mine>");
    private final ConfigData<String> addRequirement = ConfigFactory.newConfigData(
            "menus.requirements.mine-requirements.add-requirement", "<green>Add Requirement");
    private final ConfigData<String> efficiencyEnabled = ConfigFactory.newConfigData(
            "menus.requirements.mine-requirements.efficiency-enabled", "<white>Min Efficiency: <green>Enabled");
    private final ConfigData<String> efficiencyDisabled = ConfigFactory.newConfigData(
            "menus.requirements.mine-requirements.efficiency-disabled", "<white>Min Efficiency: <red>Disabled");
    private final ConfigData<String> efficiencyLore = ConfigFactory.newConfigData(
            "menus.requirements.mine-requirements.efficiency-lore", "<gray>Requires Level <white><level>");
    private final ConfigData<String> permissionEnabled = ConfigFactory.newConfigData(
            "menus.requirements.mine-requirements.permission-enabled", "<white>Permission: <green>Enabled");
    private final ConfigData<String> permissionDisabled = ConfigFactory.newConfigData(
            "menus.requirements.mine-requirements.permission-disabled", "<white>Permission: <red>Disabled");
    private final ConfigData<String> permissionLore = ConfigFactory.newConfigData(
            "menus.requirements.mine-requirements.permission-lore", "<gray>Node: <white><node>");
    private final ConfigData<String> unknownRequirement = ConfigFactory.newConfigData(
            "menus.requirements.mine-requirements.unknown-requirement", "<white><type>");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public MineRequirementsGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(addRequirement);
        configManager.registerLang(efficiencyEnabled);
        configManager.registerLang(efficiencyDisabled);
        configManager.registerLang(efficiencyLore);
        configManager.registerLang(permissionEnabled);
        configManager.registerLang(permissionDisabled);
        configManager.registerLang(permissionLore);
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
                        .asGuiItem(event -> guiManager.getAddMineRequirementGUI().open(player, mine)));

        for (IMineRequirement requirement : mine.getMineRequirements()) {
            gui.addItem(buildItem(player, mine, requirement));
        }

        gui.open(player);
    }

    private GuiItem buildItem(Player player, BasicMine mine, IMineRequirement requirement) {

        if (requirement instanceof EfficiencyMineRequirement efficiency) {
            boolean enabled = efficiency.isEnabled();
            return ItemBuilder.from(Material.GOLDEN_PICKAXE)
                    .name(MessageUtils.format(enabled ? efficiencyEnabled : efficiencyDisabled).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .lore(List.of(
                            MessageUtils.format(efficiencyLore, "level", String.valueOf(efficiency.getEfficiencyLevel()))
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            Component.empty(),
                            MessageUtils.format(MenuCommonText.LEFT_CLICK_EDIT).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            MessageUtils.format(MenuCommonText.SHIFT_RIGHT_CLICK_REMOVE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    ))
                    .asGuiItem(event -> {
                        if (event.getClick() == ClickType.SHIFT_RIGHT) {
                            mine.removeMineRequirement(efficiency);
                            Bukkit.getScheduler().runTask(plugin, () -> open(player, mine));
                            return;
                        }
                        guiManager.getMinEfficiencyGUI().open(player, mine);
                    });
        }

        if (requirement instanceof PermissionMineRequirement permission) {
            boolean enabled = permission.isEnabled();
            String perm = permission.getPermission();
            return ItemBuilder.from(Material.WRITABLE_BOOK)
                    .name(MessageUtils.format(enabled ? permissionEnabled : permissionDisabled).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                    .lore(List.of(
                            MessageUtils.format(permissionLore, "node", perm.isEmpty() ? MessageUtils.plainFormat(MenuCommonText.NOT_SET) : perm)
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            Component.empty(),
                            MessageUtils.format(MenuCommonText.LEFT_CLICK_EDIT).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                            MessageUtils.format(MenuCommonText.SHIFT_RIGHT_CLICK_REMOVE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    ))
                    .asGuiItem(event -> {
                        if (event.getClick() == ClickType.SHIFT_RIGHT) {
                            mine.removeMineRequirement(permission);
                            Bukkit.getScheduler().runTask(plugin, () -> open(player, mine));
                            return;
                        }
                        guiManager.getPermissionRequirementGUI().open(player, mine);
                    });
        }

        return ItemBuilder.from(Material.PAPER)
                .name(MessageUtils.format(unknownRequirement, "type", requirement.getClass().getSimpleName())
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .asGuiItem();
    }
}
