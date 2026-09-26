package me.simplyran.simplymines.commands;

import lombok.Getter;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.commands.subcommands.*;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.updater.UpdateChecker;
import me.simplyran.simplymines.utils.MessageUtils;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainCommand implements CommandExecutor {

    static final String HELP = "help";

    private final GuiManager guiManager;

    private final ConfigData<String> onlyPlayers = ConfigFactory.newConfigData(
            "messages.only-players", "<#ffd166>SimplyMines <dark_gray>» <#ef6f6c>Only players can use this command.");
    private final ConfigData<String> unknownSubcommand = ConfigFactory.newConfigData(
            "messages.unknown-subcommand", "<#ffd166>SimplyMines <dark_gray>» <#ef6f6c>Unknown subcommand <white><input><#ef6f6c>. Try <#ffd166>/<label> help<#ef6f6c>.");
    private final ConfigData<String> noPermission = ConfigFactory.newConfigData(
            "messages.no-permission", "<#ffd166>SimplyMines <dark_gray>» <#ef6f6c>You don't have permission to do that.");

    @Getter private final List<SubCommand> subCommands;

    public MainCommand(@NotNull MineManager mineManager,
                       @NotNull GuiManager guiManager,
                       @NotNull WorkloadRunnable workloadRunnable,
                       @NotNull SelectionManager selectionManager,
                       @NotNull ConfigManager configManager,
                       @NotNull SimplyMines plugin,
                       @NotNull UpdateChecker updateChecker) {
        this.guiManager = guiManager;
        configManager.registerLang(onlyPlayers);
        configManager.registerLang(unknownSubcommand);
        configManager.registerLang(noPermission);
        CommandText.register(configManager);

        this.subCommands = new ArrayList<>();

        subCommands.add(new ReloadSubCommand(mineManager, workloadRunnable, configManager));
        subCommands.add(new ToolSubCommand(selectionManager, configManager));
        subCommands.add(new ResetSubCommand(mineManager, configManager));
        subCommands.add(new CreateSubCommand(mineManager, configManager, selectionManager, guiManager, workloadRunnable));
        subCommands.add(new DeleteSubCommand(mineManager, configManager));
        subCommands.add(new ReassignSubCommand(mineManager, configManager, selectionManager));
        subCommands.add(new EnableSubCommand(mineManager, configManager));
        subCommands.add(new DisableSubCommand(mineManager, configManager));
        subCommands.add(new TeleportSubCommand(mineManager, configManager, guiManager));
        subCommands.add(new SetTeleportSubCommand(mineManager, configManager));
        subCommands.add(new SaveSubCommand(mineManager, configManager));
        subCommands.add(new EditSubCommand(mineManager, configManager, guiManager));
        subCommands.add(new RenameSubCommand(mineManager, configManager));
        subCommands.add(new VersionSubCommand(configManager, plugin, updateChecker));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {

        if (args.length == 0) {
            if (sender instanceof Player player && sender.hasPermission("simplymines.admin")) {
                guiManager.getMainMenuGUI().open(player);
            } else {
                sendHelp(sender, label);
            }
            return true;
        }

        String name = args[0];
        if (name.equalsIgnoreCase(HELP)) {
            sendHelp(sender, label);
            return true;
        }

        SubCommand subCommand = subCommands.stream()
                .filter(sub -> sub.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (subCommand == null) {
            sender.sendMessage(MessageUtils.format(sender, unknownSubcommand, "input", name, "label", label));
        } else if (!sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(MessageUtils.format(sender, noPermission));
        } else if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(MessageUtils.format(sender, onlyPlayers));
        } else {
            subCommand.preform(sender, args, label);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        if (subCommands.stream().noneMatch(sub -> sender.hasPermission(sub.getPermission()))) {
            sender.sendMessage(MessageUtils.format(sender, noPermission));
            return;
        }
        CommandText.sendHelp(sender, label, subCommands);
    }
}
