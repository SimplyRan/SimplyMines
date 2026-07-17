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
public class EnableSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final ConfigManager configManager;

    @Override
    public String getName() {
        return "enable";
    }

    @Override
    public String getPermission() {
        return "simplymines.enable";
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
        if (mine == null) {
            sender.sendMessage(configManager.getMessage("mine-not-found", "%mine%", mineName));
        } else {
            mine.setEnabled(true);
            sender.sendMessage(configManager.getMessage("mine-enabled", "%mine%", mineName));
        }

    }
}
