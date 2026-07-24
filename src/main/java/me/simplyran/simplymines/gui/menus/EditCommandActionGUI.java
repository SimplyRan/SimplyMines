package me.simplyran.simplymines.gui.menus;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.impl.CommandAction;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.ChatInputManager;
import me.simplyran.simplymines.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

/**
 * Editor for a single {@link CommandAction} instance attached to a block.
 */
public class EditCommandActionGUI {

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public EditCommandActionGUI(SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;
    }

    public void open(Player player, String block, BasicMine mine, CommandAction action) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(Component.text("Edit Command Action"))
                .disableAllInteractions()
                .create();

        gui.setCloseGuiAction(event -> {
            if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW
                    || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
            if (action.getCommandName().isEmpty()) {
                mine.removeAction(block, action);
            }
            mineManager.saveMineAsync(mine);
            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockActionsGUI().open(player, block, mine));
        });

        GuiUtils.fillBorder(gui);

        gui.setItem(3, 1,
                ItemBuilder.from(Material.ARROW)
                        .name(Component.text("Back").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(1, 3,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(Component.text("Set Command").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.YELLOW))
                        .lore(Component.text("Click, then type the command in chat").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                                Component.text("e.g. give %player% diamond 1").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.DARK_GRAY))
                        .asGuiItem(event -> {
                            event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            player.sendMessage(Component.text("Type the command in chat (without leading /), or 'cancel'.").color(NamedTextColor.YELLOW));

                            ChatInputManager.awaitInput(player, input -> {
                                if (!input.equalsIgnoreCase("cancel")) {
                                    String trimmed = input.trim().startsWith("/") ? input.trim().substring(1) : input.trim();
                                    String[] parts = trimmed.split(" ");
                                    String commandName = parts[0];
                                    String[] args = parts.length > 1
                                            ? List.of(parts).subList(1, parts.length).toArray(new String[0])
                                            : new String[0];
                                    action.setCommand(commandName, args);
                                    if (Bukkit.getCommandMap().getCommand(commandName) == null) {
                                        player.sendMessage(Component.text("Warning: '" + commandName + "' is not a registered command, it will not run.").color(NamedTextColor.RED));
                                    } else {
                                        player.sendMessage(Component.text("Command set to " + trimmed).color(NamedTextColor.GREEN));
                                    }
                                }
                                Bukkit.getScheduler().runTask(plugin, () -> open(player, block, mine, action));
                            });
                        }));

        gui.setItem(1, 7,
                ItemBuilder.from(Material.BARRIER)
                        .name(Component.text("Remove Action").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.RED))
                        .asGuiItem(event -> {
                            mine.removeAction(block, action);
                            mineManager.saveMineAsync(mine);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockActionsGUI().open(player, block, mine));
                        }));

        renderDisplay(gui, action);

        new ToggleButton(gui, 2, 5, "Run As Console",
                action::isAsConsole, action::setAsConsole, null).render();

        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, "Remove 10% Chance", NamedTextColor.RED,
                delta -> adjust(gui, action, -delta / 100.0)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, "Remove 5% Chance", NamedTextColor.RED,
                delta -> adjust(gui, action, -delta / 100.0)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, "Remove 1% Chance", NamedTextColor.RED,
                delta -> adjust(gui, action, -delta / 100.0)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, "Add 1% Chance", NamedTextColor.GREEN,
                delta -> adjust(gui, action, delta / 100.0)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, "Add 5% Chance", NamedTextColor.GREEN,
                delta -> adjust(gui, action, delta / 100.0)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, "Add 10% Chance", NamedTextColor.GREEN,
                delta -> adjust(gui, action, delta / 100.0)).render();

        gui.open(player);
    }

    private void adjust(Gui gui, CommandAction action, double delta) {
        action.setChance(action.getChance() + delta);
        renderDisplay(gui, action);
        gui.update();
    }

    private void renderDisplay(Gui gui, CommandAction action) {
        int chancePercent = (int) Math.round(action.getChance() * 100);
        String commandLine = action.getCommandName().isEmpty()
                ? "(not set)"
                : action.getCommandName() + " " + String.join(" ", action.getArgs());

        gui.setItem(1, 5,
                ItemBuilder.from(Material.COMMAND_BLOCK)
                        .name(Component.text("Command Display").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .lore(Component.text(commandLine.trim()).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY),
                                Component.text("Chance: " + chancePercent + "%").decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.WHITE))
                        .asGuiItem());
    }
}
