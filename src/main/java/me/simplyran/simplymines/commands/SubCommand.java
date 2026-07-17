package me.simplyran.simplymines.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SubCommand {

    String getName();

    String getPermission();

    boolean isPlayerOnly();

    List<String> tabcomplete();

    void preform(@NotNull CommandSender sender, @NotNull String[] args, String mainCommandName);



}
