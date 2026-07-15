package me.simplyran.simplymines.commands;

import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.impl.BasicMine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MainCommandTabComplete implements TabCompleter {


    private final MineManager mineManager;

    public MainCommandTabComplete(@NotNull MineManager mineManager){
        this.mineManager = mineManager;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String @NotNull [] args) {

        if (args.length <= 1) return List.of("create", "reset", "delete", "disable", "enable", "reload", "teleport", "setteleport", "reassign", "tool");
        if (args.length == 2){
            String arg1 = args[0];
            if (arg1.equalsIgnoreCase("delete")
                    || arg1.equalsIgnoreCase("disable")
                    || arg1.equalsIgnoreCase("enable")
                    || arg1.equalsIgnoreCase("reset")
                    || arg1.equalsIgnoreCase("teleport")
                    || arg1.equalsIgnoreCase("setteleport")
                    || arg1.equalsIgnoreCase("reassign")
            ){
                List<String> minesNames = new ArrayList<>();
                for (BasicMine mine : mineManager.getMines()){
                    minesNames.add(mine.getName());
                }
                return minesNames;
            }
        }

        return List.of("");

    }
}
