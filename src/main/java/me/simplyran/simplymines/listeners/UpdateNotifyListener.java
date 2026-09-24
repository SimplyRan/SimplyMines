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

    private final String updateAvailable =
                      "<gradient:#FFD700:#FF8C00><bold>[SimplyMines]</bold></gradient> <gray>An update is available: "
                    + "<green><bold><latest></bold></green> <dark_gray>(<white>you're on <current></white>)</dark_gray>"
                    + "<newline><gray><italic><click:open_url:'" + UpdateChecker.PROJECT_PAGE_URL + "'>"
                    + "<hover:show_text:'<yellow>Click to download the latest version'><aqua><underlined>Click here to update!</underlined></aqua></hover></click></italic>";

    private final UpdateChecker updateChecker;

    public UpdateNotifyListener(@NotNull UpdateChecker updateChecker) {
        this.updateChecker = updateChecker;
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
