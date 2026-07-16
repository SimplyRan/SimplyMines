package me.simplyran.simplymines.utils;

import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.objects.BasicMine;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WarnUtils {

    public static void checkWarnings(@NotNull BasicMine mine, long now, @NotNull ConfigManager configManager) {
        long secondsUntilReset = mine.getResetTime() - (now - mine.getLastReset());
        if (secondsUntilReset <= 0) return;

        int seconds = (int) secondsUntilReset;
        if (!mine.getWarnSeconds().contains(seconds)) return;
        if (!mine.getWarnedSeconds().add(seconds)) return; // already warned this second

        String secondsStr = String.valueOf(seconds);

        if (mine.isWarnGlobal()) {
            Component message = configManager.getMessage("warn-global",
                    "mine", mine.getName(), "seconds", secondsStr);
            Bukkit.broadcast(message);
        } else if (mine.isWarnNear()) {
            Component message = configManager.getMessage("warn-near",
                    "mine", mine.getName(), "seconds", secondsStr);
            warnNearbyPlayers(mine, message);
        }
    }

    private static void warnNearbyPlayers(@NotNull BasicMine mine, @NotNull Component message) {
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