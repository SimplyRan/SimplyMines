package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MineSaver;
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

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public MineRequirementsGUI(SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("Mine Requirements: " + mine.getName()))
                .rows(2)
                .pageSize(9)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            MineSaver.saveAsync(plugin, mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineEditorGUI().open(player, mine.getName()));
        });

        GuiUtils.fillRow(gui, 2, Material.WHITE_STAINED_GLASS_PANE);

        gui.setItem(2, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(2, 3,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Previous").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.previous()));

        gui.setItem(2, 7,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Next").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> gui.next()));

        gui.setItem(2, 9,
                ItemBuilder.from(Material.EMERALD)
                        .name(Component.text("Add Requirement").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN))
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
                    .name(Component.text("Min Efficiency: ").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE)
                            .append(Component.text(enabled ? "Enabled" : "Disabled").color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)))
                    .lore(List.of(
                            Component.text("Requires Level ").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY)
                                    .append(Component.text(efficiency.getEfficiencyLevel()).color(NamedTextColor.WHITE)),
                            Component.empty(),
                            Component.text("Left click to edit").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                            Component.text("Shift-right click to remove").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED)
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
                    .name(Component.text("Permission: ").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE)
                            .append(Component.text(enabled ? "Enabled" : "Disabled").color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)))
                    .lore(List.of(
                            Component.text("Node: ").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY)
                                    .append(Component.text(perm.isEmpty() ? "(not set)" : perm).color(NamedTextColor.WHITE)),
                            Component.empty(),
                            Component.text("Left click to edit").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                            Component.text("Shift-right click to remove").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED)
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
                .name(Component.text(requirement.getClass().getSimpleName())
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.WHITE))
                .asGuiItem();
    }
}