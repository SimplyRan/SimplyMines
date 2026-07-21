package me.simplyran.simplymines.utils;

import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WarnUtils {

    private final ConfigData<String> warnGlobal = ConfigFactory.newConfigData(
            "messages.warn-global", "<yellow><mine> <gray>resets in <red><seconds>s<gray> (server-wide)!");
    private final ConfigData<String> warnNear = ConfigFactory.newConfigData(
            "messages.warn-near", "<yellow><mine> <gray>resets nearby in <red><seconds>s<gray>!");

    public WarnUtils(@NotNull ConfigManager configManager) {
        configManager.register(warnGlobal);
        configManager.register(warnNear);
    }

    public void checkWarnings(@NotNull BasicMine mine, long now) {
        TimeResetRequirement timeReq = mine.getResetRequirement(TimeResetRequirement.class);
        if (timeReq == null) return;

        long secondsUntilReset = timeReq.getResetTime() - (now - timeReq.getLastReset());
        if (secondsUntilReset <= 0) return;

        int seconds = (int) secondsUntilReset;
        if (!mine.getWarnSeconds().contains(seconds)) return;
        if (!mine.getWarnedSeconds().add(seconds)) return;

        String secondsStr = String.valueOf(seconds);

        if (mine.isWarnGlobal()) {
            Component message = MessageUtils.format(warnGlobal,
                    "mine", mine.getName(),
                    "seconds", secondsStr);

            Bukkit.broadcast(message);
        } else if (mine.isWarnNear()) {
            Component message = MessageUtils.format(warnNear,
                    "mine", mine.getName(),
                    "seconds", secondsStr);

            warnNearbyPlayers(mine, message);
        }
    }

    private void warnNearbyPlayers(@NotNull BasicMine mine, @NotNull Component message) {
        BoxedRegion region = mine.getRegion();
        double distSq = (double) mine.getWarnDistance() * mine.getWarnDistance();

        double cx = (region.getMinX() + region.getMaxX()) / 2.0;
        double cy = (region.getMinY() + region.getMaxY()) / 2.0;
        double cz = (region.getMinZ() + region.getMaxZ()) / 2.0;

        for (Player player : region.getWorld().getPlayers()) {
            Location loc = player.getLocation();
            double dx = loc.getX() - cx, dy = loc.getY() - cy, dz = loc.getZ() - cz;
            if (dx * dx + dy * dy + dz * dz <= distSq) {
                player.sendMessage(message);
            }
        }
    }
}
