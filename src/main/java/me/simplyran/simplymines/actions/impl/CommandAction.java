package me.simplyran.simplymines.actions.impl;

import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandAction implements IAction {

    private final Command command;
    private final String[] args;

    private CommandAction(Command command,
                          String[] args){
        this.command = command;
        this.args = args;
    }

    @Override
    public void perform(@NotNull Location location, @NotNull BasicMine mine, @NotNull Player player) {
        command.execute(player, command.getLabel(), args);
    }





}
