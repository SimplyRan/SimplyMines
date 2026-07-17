package me.simplyran.simplymines.commands.subcommands;

import lombok.AllArgsConstructor;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

@AllArgsConstructor
public class ResetSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final ConfigManager configManager;

    @Override
    public String getName() {
        return "reset";
    }

    @Override
    public String getPermission() {
        return "simplymines.reset";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public List<String> tabcomplete() {
        return mineManager.getMinesNames();
    }


    @Override
    public void preform(@NotNull CommandSender sender, @NonNull @NotNull String[] args, String mainCommandName) {
        if (args.length < 2) {
            sender.sendMessage(configManager.getMessage("missing-mine-name", "%sub%", getName(), "%label%", mainCommandName));
            return;
        }

        String mineName = args[1];

        BasicMine mine = mineManager.getMine(mineName);
        if (mine != null) {
            mine.reset();
            sender.sendMessage(configManager.getMessage("mine-reset", "%mine%", mineName));
        } else {
            sender.sendMessage(configManager.getMessage("mine-not-found", "%mine%", mineName));
        }

    }

}
