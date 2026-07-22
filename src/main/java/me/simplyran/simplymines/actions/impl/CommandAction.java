package me.simplyran.simplymines.actions.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandAction implements IAction {

    public final static String NAME = "command_action";

    @Getter private String commandName;
    private Command command;
    @Getter private String[] args;
    private double chance;

    private CommandAction(String commandName,
                          Command command,
                          String[] args,
                          double chance){
        this.commandName = commandName;
        this.command = command;
        this.args = args;
        this.chance = chance;
    }

    public CommandAction(String commandName, Command command, String[] args){
        this(commandName, command, args, 1.0);
    }

    @Override
    public void perform(@NotNull Location location, @NotNull BasicMine mine, @NotNull Player player) {
        if (command == null) return;
        command.execute(player, command.getLabel(), args);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public double getChance() {
        return chance;
    }

    @Override
    public void setChance(double chance) {
        this.chance = Math.clamp(chance, 0, 1);
    }

    public void setCommand(String commandName, String[] args) {
        this.commandName = commandName;
        this.command = Bukkit.getCommandMap().getCommand(commandName);
        this.args = args;
    }

    @Override
    public List<Pair<String, Object>> serialize() {
        return List.of(Pair.of("command_name", commandName),
                        Pair.of("args", List.of(args)),
                        Pair.of("chance", chance));
    }

    @Nullable
    public static IAction deserialize(@NotNull JsonObject json) {
        if (!json.has("command_name") || !json.has("args")) {
            return null;
        }

        String commandName = json.get("command_name").getAsString();
        Command command = Bukkit.getCommandMap().getCommand(commandName);

        List<String> argList = new ArrayList<>();
        for (JsonElement element : json.get("args").getAsJsonArray()) {
            argList.add(element.getAsString());
        }
        String[] args = argList.toArray(new String[0]);

        double chance = json.has("chance") ? json.get("chance").getAsDouble() : 1.0;

        return new CommandAction(commandName, command, args, chance);
    }


}
