package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.updater.UpdateCheckResult;
import me.simplyran.simplymines.updater.UpdateChecker;
import me.simplyran.simplymines.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class UpdateNotifyListener implements Listener {

    private final ConfigData<String> updateAvailable = ConfigFactory.newConfigData(
            "messages.update-available", "<yellow>[SimplyMines] Update available: <latest> (you're on <current>).");

    private final UpdateChecker updateChecker;

    public UpdateNotifyListener(@NotNull ConfigManager configManager, @NotNull UpdateChecker updateChecker) {
        this.updateChecker = updateChecker;
        configManager.registerLang(updateAvailable);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("simplymines.update-notify")) return;

        updateChecker.getLastResult()
                .filter(UpdateCheckResult::updateAvailable)
                .ifPresent(result -> player.sendMessage(MessageUtils.format(player, updateAvailable,
                        "current", result.currentVersion(),
                        "latest", result.latestVersion())));
    }
}
