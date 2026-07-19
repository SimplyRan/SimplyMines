package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.objects.ConfigFactory;
import me.simplyran.simplymines.utils.MessageUtils;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ReloadSubCommand implements SubCommand {

    private final MineManager mineManager;
    private final WorkloadRunnable workloadRunnable;
    private final ConfigManager configManager;

    private final ConfigData<String> reloaded = ConfigFactory.newConfigData(
            "messages.reloaded", "<green>Mines and Config have been reloaded!");

    public ReloadSubCommand(@NotNull MineManager mineManager,
                            @NotNull WorkloadRunnable workloadRunnable,
                            @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        this.workloadRunnable = workloadRunnable;
        this.configManager = configManager;
        configManager.register(reloaded);
    }

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
        sender.sendMessage(MessageUtils.format(sender, reloaded));

    }
}
