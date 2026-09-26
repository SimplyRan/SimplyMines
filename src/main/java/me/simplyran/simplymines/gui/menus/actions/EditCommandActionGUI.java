package me.simplyran.simplymines.gui.menus.actions;

import dev.triumphteam.gui.guis.Gui;
import me.simplyran.simplymines.actions.impl.CommandAction;
import me.simplyran.simplymines.gui.Btn;
import me.simplyran.simplymines.gui.Menu;
import me.simplyran.simplymines.gui.MenuCommonText;
import me.simplyran.simplymines.gui.MenuServices;
import me.simplyran.simplymines.gui.buttons.ToggleButton;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.ChatInputManager;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

public class EditCommandActionGUI extends Menu {

    private static final String PATH = "menus.actions.edit-command.";

    private final ConfigData<String> title = lang(PATH + "title", "<dark_gray>Edit Command Action");
    private final ConfigData<String> setCommandName = lang(PATH + "set-command-name", "<#ffd166>Set Command");
    private final ConfigData<String> setCommandLore1 = lang(PATH + "set-command-lore-1", "<#8b9bb4>Click, then type the command in chat");
    private final ConfigData<String> setCommandLore2 = lang(PATH + "set-command-lore-2", "<dark_gray>e.g. give %player% diamond 1");
    private final ConfigData<String> runAsConsoleLabel = lang(PATH + "run-as-console-label", "Run As Console");
    private final ConfigData<String> commandDisplayName = lang(PATH + "command-display-name", "<#ffd166>Command");
    private final ConfigData<String> commandDisplayLine = lang(PATH + "command-display-line", "<white><command>");

    private final ConfigData<String> promptCommand = lang("messages.action-edit-prompt-command", "<#ffd166>Type the command in chat (without leading /), or 'cancel'.");
    private final ConfigData<String> commandNotRegistered = lang("messages.action-edit-command-not-registered", "<#ef6f6c>Warning: '<command>' is not a registered command, it will not run.");
    private final ConfigData<String> commandSet = lang("messages.action-edit-command-set", "<#7bd88f>Command set to <command>");

    public EditCommandActionGUI(MenuServices services) {
        super(services);
    }

    public void open(Player player, String block, BasicMine mine, CommandAction action) {
        Runnable back = () -> services.guiManager().getBlockActionsGUI().open(player, block, mine);
        Runnable self = () -> open(player, block, mine, action);
        Gui gui = createGui(5, MessageUtils.format(title), back, () -> {
            if (action.getCommandName().isEmpty()) mine.removeAction(block, action);
            services.mineManager().saveMineAsync(mine);
        });

        gui.setItem(2, 3, Btn.of(Material.WRITABLE_BOOK, setCommandName)
                .lore(setCommandLore1)
                .lore(setCommandLore2)
                .onClick(event -> {
                    player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                    player.sendMessage(MessageUtils.format(player, promptCommand));

                    ChatInputManager.awaitInput(player, input -> {
                        if (!input.equalsIgnoreCase("cancel")) applyCommand(player, action, input);
                        reopenLater(() -> open(player, block, mine, action));
                    });
                })
                .build());

        gui.setItem(2, 7, Btn.of(Material.BARRIER, MenuCommonText.REMOVE_ACTION)
                .onClick(event -> {
                    mine.removeAction(block, action);
                    services.mineManager().saveMineAsync(mine);
                    reopenLater(back);
                })
                .build());

        renderDisplay(gui, action);
        adjusters(gui, 3, new int[]{1, 5, 10}, MenuCommonText.ADJUST_REMOVE_PERCENT, MenuCommonText.ADJUST_ADD_PERCENT,
                delta -> adjust(gui, action, delta / 100.0),
                typeValueButton(player, self, value -> adjust(gui, action, value / 100.0 - action.getChance())));
        new ToggleButton(gui, 4, 5, MessageUtils.format(runAsConsoleLabel),
                action::isAsConsole, action::setAsConsole, null).render();

        gui.open(player);
    }

    private void applyCommand(Player player, CommandAction action, String input) {
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

    private void adjust(Gui gui, CommandAction action, double delta) {
        action.setChance(Math.clamp(action.getChance() + delta, 0, 1));
        renderDisplay(gui, action);
        gui.update();
    }

    private void renderDisplay(Gui gui, CommandAction action) {
        int chancePercent = (int) Math.round(action.getChance() * 100);
        String commandLine = action.getCommandName().isEmpty()
                ? MessageUtils.plainFormat(MenuCommonText.NOT_SET)
                : action.getCommandName() + " " + String.join(" ", action.getArgs());

        gui.setItem(2, 5, Btn.of(Material.COMMAND_BLOCK, commandDisplayName)
                .lore(commandDisplayLine, "command", commandLine.trim())
                .lore(MessageUtils.format(MenuCommonText.CHANCE_LORE, "percent", String.valueOf(chancePercent))
                        .colorIfAbsent(NamedTextColor.WHITE))
                .build());
    }
}
