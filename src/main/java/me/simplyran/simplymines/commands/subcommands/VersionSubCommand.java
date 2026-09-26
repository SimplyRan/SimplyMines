package me.simplyran.simplymines.commands.subcommands;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.commands.SubCommand;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.updater.UpdateChecker;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VersionSubCommand implements SubCommand {

    private final ConfigData<String> versionCurrent = ConfigFactory.newConfigData(
            "messages.version-current", "<#ffd166>SimplyMines <dark_gray>» <#8b9bb4>Running SimplyMines <white><current>");
    private final ConfigData<String> versionUpToDate = ConfigFactory.newConfigData(
            "messages.version-up-to-date", "<#ffd166>SimplyMines <dark_gray>» <#7bd88f>You are running the latest version.");
    private final ConfigData<String> versionUpdateAvailable = ConfigFactory.newConfigData(
            "messages.version-update-available", "<#ffd166>SimplyMines <dark_gray>» <#ffd166>Update available: <white><latest>");
    private final ConfigData<String> versionCheckFailed = ConfigFactory.newConfigData(
            "messages.version-check-failed", "<#ffd166>SimplyMines <dark_gray>» <#ef6f6c>Could not check for updates.");

    private final SimplyMines plugin;
    private final UpdateChecker updateChecker;

    public VersionSubCommand(@NotNull ConfigManager configManager,
                             @NotNull SimplyMines plugin,
                             @NotNull UpdateChecker updateChecker) {
        this.plugin = plugin;
        this.updateChecker = updateChecker;

        configManager.registerLang(versionCurrent);
        configManager.registerLang(versionUpToDate);
        configManager.registerLang(versionUpdateAvailable);
        configManager.registerLang(versionCheckFailed);
    }

    @Override
    public String getName() {
        return "version";
    }

    @Override
    public String getPermission() {
        return "simplymines.version";
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
    public void preform(@NotNull CommandSender sender, @NotNull String[] args, String mainCommandName) {
        sender.sendMessage(MessageUtils.format(sender, versionCurrent, "current", plugin.getDescription().getVersion()));

        updateChecker.checkAsync().thenAccept(result -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (result == null || result.checkFailed()) {
                sender.sendMessage(MessageUtils.format(sender, versionCheckFailed));
            } else if (result.updateAvailable()) {
                sender.sendMessage(MessageUtils.format(sender, versionUpdateAvailable, "latest", result.latestVersion()));
            } else {
                sender.sendMessage(MessageUtils.format(sender, versionUpToDate));
            }
        }));
    }
}
