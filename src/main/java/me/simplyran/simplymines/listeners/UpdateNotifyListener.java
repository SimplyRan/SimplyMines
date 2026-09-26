package me.simplyran.simplymines.listeners;

import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.updater.UpdateCheckResult;
import me.simplyran.simplymines.updater.UpdateChecker;
import me.simplyran.simplymines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class UpdateNotifyListener implements Listener {

    private final ConfigData<String> updateAvailable = ConfigFactory.newConfigData(
            "messages.update-available",
            "<#ffd166>SimplyMines <dark_gray>» <#8b9bb4>An update is available: <#7bd88f><latest> <dark_gray>(<white>you're on <current><dark_gray>)");
    private final ConfigData<String> updateClick = ConfigFactory.newConfigData(
            "messages.update-click", "<#8b9bb4>» <#ffd166><underlined>Click here to update!");
    private final ConfigData<String> updateHover = ConfigFactory.newConfigData(
            "messages.update-hover", "<#8b9bb4>Click to open the download page");

    private final UpdateChecker updateChecker;

    public UpdateNotifyListener(@NotNull UpdateChecker updateChecker, @NotNull ConfigManager configManager) {
        this.updateChecker = updateChecker;
        configManager.registerLang(updateAvailable);
        configManager.registerLang(updateClick);
        configManager.registerLang(updateHover);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("simplymines.update-notify")) return;

        updateChecker.getLastResult()
                .filter(UpdateCheckResult::updateAvailable)
                .ifPresent(result -> {
                    Component click = MessageUtils.format(player, updateClick)
                            .clickEvent(ClickEvent.openUrl(UpdateChecker.PROJECT_PAGE_URL))
                            .hoverEvent(HoverEvent.showText(MessageUtils.format(player, updateHover)));

                    player.sendMessage(MessageUtils.format(player, updateAvailable,
                                    "current", result.currentVersion(),
                                    "latest", result.latestVersion())
                            .appendNewline()
                            .append(click));
                });
    }
}
