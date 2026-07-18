package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
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
 * Hub listing every IResetRequirement attached to a mine.
 * Left click a requirement to edit it, shift-right click to remove it,
 * or click "Add Requirement" to attach a new type.
 */
public class ResetRequirementsGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public ResetRequirementsGUI(SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void open(Player player, BasicMine mine) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("Reset Requirements: " + mine.getName()))
                .rows(2)
                .pageSize(9)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;

            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getMineEditorGUI().open(player, mine.getName()));
            MineSaver.saveAsync(plugin, mine);
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
                        .asGuiItem(event -> guiManager.getAddResetRequirementGUI().open(player, mine)));

        for (IResetRequirement requirement : mine.getResetRequirements()) {
            gui.addItem(buildItem(player, mine, requirement));
        }

        gui.open(player);
    }

    private GuiItem buildItem(Player player, BasicMine mine, IResetRequirement requirement) {

        if (requirement instanceof TimeResetRequirement time) {
            return ItemBuilder.from(Material.CLOCK)
                    .name(Component.text("Time Reset").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                    .lore(List.of(
                            Component.text("Resets every ").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY)
                                    .append(Component.text(time.getResetTime() + "s").color(NamedTextColor.WHITE)),
                            Component.empty(),
                            Component.text("Left click to edit").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                            Component.text("Shift-right click to remove").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED)
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
                    .name(Component.text("Percent Reset: ").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE)
                            .append(Component.text(enabled ? "Enabled" : "Disabled").color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)))
                    .lore(List.of(
                            Component.text("Resets at ").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY)
                                    .append(Component.text(percent.getResetAtPercentage() + "% left").color(NamedTextColor.WHITE)),
                            Component.empty(),
                            Component.text("Left click to edit").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                            Component.text("Shift-right click to remove").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED)
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

        // Fallback for any future requirement type without a dedicated editor yet
        return ItemBuilder.from(Material.PAPER)
                .name(Component.text(requirement.getClass().getSimpleName())
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.WHITE))
                .asGuiItem();
    }
}