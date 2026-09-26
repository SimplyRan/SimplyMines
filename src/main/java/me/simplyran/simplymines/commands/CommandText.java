package me.simplyran.simplymines.commands;

import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Messages shared by every subcommand, plus the /sm help output. */
public final class CommandText {

    public static final ConfigData<String> MINE_NOT_FOUND = ConfigFactory.newConfigData(
            "messages.mine-not-found", "<#ffd166>SimplyMines <dark_gray>» <#ef6f6c>Mine <white><mine> <#ef6f6c>was not found.");
    public static final ConfigData<String> MINE_ALREADY_EXISTS = ConfigFactory.newConfigData(
            "messages.mine-already-exists", "<#ef6f6c>Mine <white><mine> <#ef6f6c>already exists.");
    public static final ConfigData<String> INVALID_MINE_NAME = ConfigFactory.newConfigData(
            "messages.invalid-mine-name", "<#ffd166>SimplyMines <dark_gray>» <#ef6f6c>Invalid mine name. Use only letters, numbers, - and _ (max 32 characters).");
    public static final ConfigData<String> COMMAND_USAGE = ConfigFactory.newConfigData(
            "messages.command-usage", "<#ffd166>SimplyMines <dark_gray>» <#ef6f6c>Usage: <white>/<label> <sub> <#ffd166><args>");

    public static final ConfigData<String> HELP_HEADER = ConfigFactory.newConfigData(
            "messages.help-header", "<#ffd166>SimplyMines <dark_gray>» <#8b9bb4>Commands <dark_gray>(click one to fill it in)");
    public static final ConfigData<String> HELP_LINE = ConfigFactory.newConfigData(
            "messages.help-line", " <dark_gray>▸ <#ffd166>/<label> <sub> <#8b9bb4><args><dark_gray> - <white><description>");
    public static final ConfigData<String> HELP_HOVER = ConfigFactory.newConfigData(
            "messages.help-hover", "<#8b9bb4>Click to use <#ffd166>/<label> <sub>");

    /** Subcommand -> usage arguments and its help description. Only subcommands listed here appear in /sm help. */
    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();

    private record Entry(String args, ConfigData<String> description) {}

    static {
        entry("create", "[mine]", "Create a mine from your selection");
        entry("edit", "[mine]", "Open the mine editor");
        entry("reset", "[mine]", "Reset a mine now");
        entry("delete", "[mine]", "Delete a mine");
        entry("rename", "[mine] [new name]", "Rename a mine");
        entry("reassign", "[mine]", "Move a mine to your current selection");
        entry("enable", "[mine]", "Enable a mine");
        entry("disable", "[mine]", "Disable a mine");
        entry("teleport", "[mine]", "Open the teleport menu, or teleport to a mine");
        entry("setteleport", "[mine]", "Set a mine's teleport point to where you stand");
        entry("save", "[mine]", "Save a mine to storage");
        entry("tool", "", "Toggle the selection tool");
        entry("reload", "[mines|config]", "Reload config, lang and mines");
        entry("version", "", "Show the plugin version and check for updates");
    }

    private static void entry(String name, String args, String description) {
        ENTRIES.put(name, new Entry(args, ConfigFactory.newConfigData("messages.help-description-" + name, description)));
    }

    private CommandText() {}

    public static void register(@NotNull ConfigManager configManager) {
        configManager.registerLang(MINE_NOT_FOUND);
        configManager.registerLang(MINE_ALREADY_EXISTS);
        configManager.registerLang(INVALID_MINE_NAME);
        configManager.registerLang(COMMAND_USAGE);
        configManager.registerLang(HELP_HEADER);
        configManager.registerLang(HELP_LINE);
        configManager.registerLang(HELP_HOVER);
        ENTRIES.values().forEach(entry -> configManager.registerLang(entry.description()));
    }

    public static void sendUsage(@NotNull CommandSender sender, String label, String sub) {
        Entry entry = ENTRIES.get(sub);
        sender.sendMessage(MessageUtils.format(sender, COMMAND_USAGE,
                "label", label, "sub", sub, "args", entry == null ? "" : entry.args()));
    }

    /** Lists the subcommands the sender may use; each line fills the command into chat when clicked. */
    public static void sendHelp(@NotNull CommandSender sender, String label, List<SubCommand> subCommands) {
        sender.sendMessage(MessageUtils.format(sender, HELP_HEADER));

        for (SubCommand sub : subCommands) {
            Entry entry = ENTRIES.get(sub.getName());
            if (entry == null || !sender.hasPermission(sub.getPermission())) continue;

            Component hover = MessageUtils.format(sender, HELP_HOVER, "label", label, "sub", sub.getName());
            sender.sendMessage(MessageUtils.format(sender, HELP_LINE,
                            "label", label,
                            "sub", sub.getName(),
                            "args", entry.args(),
                            "description", MessageUtils.plainFormat(entry.description()))
                    .clickEvent(ClickEvent.suggestCommand("/" + label + " " + sub.getName() + " "))
                    .hoverEvent(HoverEvent.showText(hover)));
        }
    }
}
