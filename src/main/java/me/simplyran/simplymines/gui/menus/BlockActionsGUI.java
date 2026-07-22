package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.actions.impl.CommandAction;
import me.simplyran.simplymines.actions.impl.EconomyAction;
import me.simplyran.simplymines.actions.impl.ItemDropAction;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.objects.BasicMine;
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
 * Hub listing every {@link IAction} attached to a specific block within a mine.
 * Multiple actions of the same type (e.g. several ItemDropActions) are supported,
 * since each entry is tracked by its own object reference rather than by type/index.
 */
public class BlockActionsGUI {

    private final SimplyMines plugin;
    private final GuiManager guiManager;

    public BlockActionsGUI(SimplyMines plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void open(Player player, String block, BasicMine mine) {
        PaginatedGui gui = Gui.paginated()
                .title(Component.text("Actions: " + block))
                .rows(2)
                .pageSize(9)
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            MineSaver.saveAsync(plugin, mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockOptionsGUI().open(player, block, mine));
        });

        GuiUtils.fillRow(gui, 2, Material.WHITE_STAINED_GLASS_PANE);

        gui.setItem(2, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> {
                            MineSaver.saveAsync(plugin, mine);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockOptionsGUI().open(player, block, mine));
                        }));

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
                        .name(Component.text("Add Action").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GREEN))
                        .asGuiItem(event -> guiManager.getAddBlockActionGUI().open(player, block, mine)));

        for (IAction action : List.copyOf(mine.getActions(block))) {
            gui.addItem(buildItem(player, block, mine, action));
        }

        gui.open(player);
    }

    private GuiItem buildItem(Player player, String block, BasicMine mine, IAction action) {
        int chancePercent = (int) Math.round(action.getChance() * 100);

        if (action instanceof ItemDropAction itemDrop) {
            return ItemBuilder.from(itemDrop.getItemStack())
                    .lore(List.of(
                            Component.text("Type: Item Drop").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                            Component.text("Amount: " + itemDrop.getAmount()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE),
                            Component.text("Chance: " + chancePercent + "%").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE),
                            Component.empty(),
                            Component.text("Left click to edit").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                            Component.text("Shift-right click to remove").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED)
                    ))
                    .asGuiItem(event -> handleClick(player, block, mine, action, event.getClick(),
                            () -> guiManager.getEditItemDropActionGUI().open(player, block, mine, itemDrop)));
        }

        if (action instanceof CommandAction commandAction) {
            return ItemBuilder.from(Material.COMMAND_BLOCK)
                    .name(Component.text("Command: " + (commandAction.getCommandName().isEmpty() ? "(not set)" : commandAction.getCommandName()))
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                    .lore(List.of(
                            Component.text("Chance: " + chancePercent + "%").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE),
                            Component.empty(),
                            Component.text("Left click to edit").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                            Component.text("Shift-right click to remove").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED)
                    ))
                    .asGuiItem(event -> handleClick(player, block, mine, action, event.getClick(),
                            () -> guiManager.getEditCommandActionGUI().open(player, block, mine, commandAction)));
        }

        if (action instanceof EconomyAction economyAction) {
            return ItemBuilder.from(Material.GOLD_INGOT)
                    .name(Component.text("Economy: " + economyAction.getAmount())
                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                    .lore(List.of(
                            Component.text("Chance: " + chancePercent + "%").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE),
                            Component.empty(),
                            Component.text("Left click to edit").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                            Component.text("Shift-right click to remove").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED)
                    ))
                    .asGuiItem(event -> handleClick(player, block, mine, action, event.getClick(),
                            () -> guiManager.getEditEconomyActionGUI().open(player, block, mine, economyAction)));
        }

        return ItemBuilder.from(Material.PAPER)
                .name(Component.text(action.name()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                .asGuiItem();
    }

    private void handleClick(Player player, String block, BasicMine mine, IAction action, ClickType click, Runnable openEditor) {
        if (click == ClickType.SHIFT_RIGHT) {
            mine.removeAction(block, action);
            MineSaver.saveAsync(plugin, mine);
            Bukkit.getScheduler().runTask(plugin, () -> open(player, block, mine));
            return;
        }
        if (click.isRightClick()) return;
        openEditor.run();
    }
}
