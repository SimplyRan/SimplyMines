package me.simplyran.simplymines.commands.subcommands;

import lombok.AllArgsConstructor;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

@AllArgsConstructor
public class ReloadSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final WorkloadRunnable workloadRunnable;
    private final ConfigManager configManager;


    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "simplymines.reload";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public List<String> tabcomplete() {
        return List.of();
    }


    @Override
    public void preform(@NotNull CommandSender sender, @NonNull @NotNull String[] args, String mainCommandName) {
        workloadRunnable.resetWorkloadDeque();
        mineManager.reloadMines();
        configManager.reloadConfig();
        sender.sendMessage(configManager.getMessage("reloaded"));

    }
}
