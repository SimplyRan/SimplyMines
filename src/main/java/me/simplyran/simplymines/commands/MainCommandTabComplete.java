package me.simplyran.simplymines.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MainCommandTabComplete implements TabCompleter {


    private final List<SubCommand> subCommands;

    public MainCommandTabComplete(@NotNull List<SubCommand> subCommands){
        this.subCommands = subCommands;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String @NotNull [] args) {

        if (args.length <= 1) {
            List<String> subCommandsNames = new ArrayList<>();
            for (SubCommand subCommand : subCommands){
                if (sender.hasPermission(subCommand.getPermission())) subCommandsNames.add(subCommand.getName());
            }
            return subCommandsNames;
        }
        if (args.length == 2){
            for (SubCommand subCommand : subCommands){
                if (!subCommand.getName().equals(args[0])) continue;
                if (!sender.hasPermission(subCommand.getPermission())) continue;

                return subCommand.tabcomplete();
            }
        }

        return List.of();

    }
}
