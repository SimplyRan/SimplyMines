package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.actions.impl.CommandAction;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.buttons.AdjustButton;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.ChatInputManager;
import me.simplyran.simplymines.utils.GuiUtils;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

public class EditCommandActionGUI {

    private final ConfigData<String> title = ConfigFactory.newConfigData(
            "menus.actions.edit-command.title", "Edit Command Action");
    private final ConfigData<String> setCommandName = ConfigFactory.newConfigData(
            "menus.actions.edit-command.set-command-name", "<yellow>Set Command");
    private final ConfigData<String> setCommandLore1 = ConfigFactory.newConfigData(
            "menus.actions.edit-command.set-command-lore-1", "<gray>Click, then type the command in chat");
    private final ConfigData<String> setCommandLore2 = ConfigFactory.newConfigData(
            "menus.actions.edit-command.set-command-lore-2", "<dark_gray>e.g. give %player% diamond 1");
    private final ConfigData<String> runAsConsoleLabel = ConfigFactory.newConfigData(
            "menus.actions.edit-command.run-as-console-label", "Run As Console");
    private final ConfigData<String> commandDisplayName = ConfigFactory.newConfigData(
            "menus.actions.edit-command.command-display-name", "<white>Command Display");
    private final ConfigData<String> commandDisplayLine = ConfigFactory.newConfigData(
            "menus.actions.edit-command.command-display-line", "<gray><command>");

    private final ConfigData<String> promptCommand = ConfigFactory.newConfigData(
            "messages.action-edit-prompt-command", "<yellow>Type the command in chat (without leading /), or 'cancel'.");
    private final ConfigData<String> commandNotRegistered = ConfigFactory.newConfigData(
            "messages.action-edit-command-not-registered", "<red>Warning: '<command>' is not a registered command, it will not run.");
    private final ConfigData<String> commandSet = ConfigFactory.newConfigData(
            "messages.action-edit-command-set", "<green>Command set to <command>");

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final GuiManager guiManager;

    public EditCommandActionGUI(ConfigManager configManager, SimplyMines plugin, MineManager mineManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        this.guiManager = guiManager;

        configManager.registerLang(title);
        configManager.registerLang(setCommandName);
        configManager.registerLang(setCommandLore1);
        configManager.registerLang(setCommandLore2);
        configManager.registerLang(runAsConsoleLabel);
        configManager.registerLang(commandDisplayName);
        configManager.registerLang(commandDisplayLine);
        configManager.registerLang(promptCommand);
        configManager.registerLang(commandNotRegistered);
        configManager.registerLang(commandSet);
    }

    public void open(Player player, String block, BasicMine mine, CommandAction action) {
        Gui gui = Gui.gui()
                .rows(3)
                .title(MessageUtils.format(title))
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
                        .name(MessageUtils.format(MenuCommonText.BACK).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem(event -> player.closeInventory()));

        gui.setItem(1, 3,
                ItemBuilder.from(Material.WRITABLE_BOOK)
                        .name(MessageUtils.format(setCommandName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(setCommandLore1).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                                MessageUtils.format(setCommandLore2).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> {
                            event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                            player.sendMessage(MessageUtils.format(player, promptCommand));

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
                                        player.sendMessage(MessageUtils.format(player, commandNotRegistered, "command", commandName));
                                    } else {
                                        player.sendMessage(MessageUtils.format(player, commandSet, "command", trimmed));
                                    }
                                }
                                Bukkit.getScheduler().runTask(plugin, () -> open(player, block, mine, action));
                            });
                        }));

        gui.setItem(1, 7,
                ItemBuilder.from(Material.BARRIER)
                        .name(MessageUtils.format(MenuCommonText.REMOVE_ACTION).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .asGuiItem(event -> {
                            mine.removeAction(block, action);
                            mineManager.saveMineAsync(mine);
                            Bukkit.getScheduler().runTask(plugin, () -> guiManager.getBlockActionsGUI().open(player, block, mine));
                        }));

        renderDisplay(gui, action);

        new ToggleButton(gui, 2, 5, MessageUtils.plainFormat(runAsConsoleLabel),
                action::isAsConsole, action::setAsConsole, null).render();

        new AdjustButton(gui, 2, 2, Material.RED_DYE, 10, MenuCommonText.ADJUST_REMOVE_PERCENT, NamedTextColor.RED,
                delta -> adjust(gui, action, -delta / 100.0)).render();
        new AdjustButton(gui, 2, 3, Material.RED_DYE, 5, MenuCommonText.ADJUST_REMOVE_PERCENT, NamedTextColor.RED,
                delta -> adjust(gui, action, -delta / 100.0)).render();
        new AdjustButton(gui, 2, 4, Material.RED_DYE, 1, MenuCommonText.ADJUST_REMOVE_PERCENT, NamedTextColor.RED,
                delta -> adjust(gui, action, -delta / 100.0)).render();

        new AdjustButton(gui, 2, 6, Material.LIME_DYE, 1, MenuCommonText.ADJUST_ADD_PERCENT, NamedTextColor.GREEN,
                delta -> adjust(gui, action, delta / 100.0)).render();
        new AdjustButton(gui, 2, 7, Material.LIME_DYE, 5, MenuCommonText.ADJUST_ADD_PERCENT, NamedTextColor.GREEN,
                delta -> adjust(gui, action, delta / 100.0)).render();
        new AdjustButton(gui, 2, 8, Material.LIME_DYE, 10, MenuCommonText.ADJUST_ADD_PERCENT, NamedTextColor.GREEN,
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
                ? MessageUtils.plainFormat(MenuCommonText.NOT_SET)
                : action.getCommandName() + " " + String.join(" ", action.getArgs());

        gui.setItem(1, 5,
                ItemBuilder.from(Material.COMMAND_BLOCK)
                        .name(MessageUtils.format(commandDisplayName).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .lore(MessageUtils.format(commandDisplayLine, "command", commandLine.trim())
                                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                                MessageUtils.format(MenuCommonText.CHANCE_LORE, "percent", String.valueOf(chancePercent))
                                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).colorIfAbsent(NamedTextColor.WHITE))
                        .asGuiItem());
    }
}
