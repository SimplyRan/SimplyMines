package me.simplyran.simplymines.commands;

import lombok.Getter;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.commands.subcommands.*;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.GuiManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainCommand implements CommandExecutor {

    private final GuiManager guiManager;
    private final ConfigManager configManager;

    @Getter private final List<SubCommand> subCommands;

    public MainCommand(@NotNull MineManager mineManager,
                       @NotNull GuiManager guiManager,
                       @NotNull WorkloadRunnable workloadRunnable,
                       @NotNull SelectionManager selectionManager,
                       @NotNull ConfigManager configManager,
                       @NotNull SimplyMines plugin) {
        this.guiManager = guiManager;
        this.configManager = configManager;

        this.subCommands = new ArrayList<>();

        subCommands.add(new ReloadSubCommand(mineManager, workloadRunnable, configManager));
        subCommands.add(new ToolSubCommand(selectionManager, configManager));
        subCommands.add(new ResetSubCommand(mineManager, configManager));
        subCommands.add(new CreateSubCommand(mineManager, configManager, selectionManager, guiManager, workloadRunnable));
        subCommands.add(new DeleteSubCommand(mineManager, configManager));
        subCommands.add(new ReassignSubCommand(mineManager, configManager, selectionManager, plugin));
        subCommands.add(new EnableSubCommand(mineManager, configManager));
        subCommands.add(new DisableSubCommand(mineManager, configManager));
        subCommands.add(new TeleportSubCommand(mineManager, configManager));
        subCommands.add(new SetTeleportSubCommand(mineManager, configManager));
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {

        if (args.length>0){
            String subCommandName = args[0];
            boolean foundCmd = false;

            for (SubCommand subCommand : subCommands){
                if (subCommand.getName().equals(subCommandName)
                        && sender.hasPermission(subCommand.getPermission())){
                    if (!subCommand.isPlayerOnly()) subCommand.preform(sender, args, label);
                    else if (sender instanceof Player){
                        subCommand.preform(sender, args, label);
                    }
                    else {
                        sender.sendMessage(configManager.getMessage("only-players"));
                    }
                    foundCmd = true;
                    break;
                }
            }
            if (!foundCmd){
                sender.sendMessage(configManager.getMessage("unknown-subcommand", "%input%", subCommandName));
                return true;
            }

        }
        else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(configManager.getMessage("only-players"));
                return true;
            }
            if (!sender.hasPermission("simplymines.admin")) {
                sender.sendMessage(configManager.getMessage("no-permission"));
                return true;
            }
            guiManager.getMainMenuGUI().open(player);

        }
        return true;
    }

}