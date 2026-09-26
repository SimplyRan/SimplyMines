package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.commands.CommandText;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.smelting.SmeltRegistry;
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
            "messages.reloaded", "<#ffd166>SimplyMines <dark_gray>» <#7bd88f>Mines and config reloaded.");
    private final ConfigData<String> reloadedMines = ConfigFactory.newConfigData(
            "messages.reloaded-mines", "<#ffd166>SimplyMines <dark_gray>» <#7bd88f>Mines reloaded.");
    private final ConfigData<String> reloadedConfig = ConfigFactory.newConfigData(
            "messages.reloaded-config", "<#ffd166>SimplyMines <dark_gray>» <#7bd88f>Config and lang reloaded.");

    public ReloadSubCommand(@NotNull MineManager mineManager,
                            @NotNull WorkloadRunnable workloadRunnable,
                            @NotNull ConfigManager configManager) {
        this.mineManager = mineManager;
        this.workloadRunnable = workloadRunnable;
        this.configManager = configManager;
        configManager.registerLang(reloaded);
        configManager.registerLang(reloadedMines);
        configManager.registerLang(reloadedConfig);
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
        return List.of("mines", "config");
    }


    @Override
    public void preform(@NotNull CommandSender sender, @NonNull @NotNull String[] args, String mainCommandName) {
        String target = args.length > 1 ? args[1].toLowerCase() : "all";

        switch (target) {
            case "all" -> {
                reloadConfig();
                reloadMines();
                sender.sendMessage(MessageUtils.format(sender, reloaded));
            }
            case "mines" -> {
                reloadMines();
                sender.sendMessage(MessageUtils.format(sender, reloadedMines));
            }
            case "config" -> {
                reloadConfig();
                sender.sendMessage(MessageUtils.format(sender, reloadedConfig));
            }
            default -> CommandText.sendUsage(sender, mainCommandName, getName());
        }
    }

    private void reloadMines() {
        workloadRunnable.resetWorkloadDeque();
        mineManager.reloadMines();
    }

    private void reloadConfig() {
        configManager.reloadConfig();
        SmeltRegistry.rebuild();
    }
}
