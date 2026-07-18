package me.simplyran.simplymines.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MainCommandTabComplete implements TabCompleter {


    private final List<SubCommand> subCommands;

    public MainCommandTabComplete(@NotNull List<SubCommand> subCommands){
        this.subCommands = subCommands;
    }


    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender,
                                      @NonNull Command command,
                                      @NonNull String alias,
                                      String[] args) {

        if (args.length <= 1) {
            String input = args.length == 0 ? "" : args[0].toLowerCase();

            return subCommands.stream()
                    .filter(sub -> sender.hasPermission(sub.getPermission()))
                    .map(SubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .toList();
        }

        if (args.length == 2) {
            return subCommands.stream()
                    .filter(sub -> sender.hasPermission(sub.getPermission()))
                    .filter(sub -> sub.getName().equalsIgnoreCase(args[0]))
                    .findFirst()
                    .map(SubCommand::tabcomplete)
                    .orElse(List.of());
        }

        return List.of();
    }
}
