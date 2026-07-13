package me.simplyran.simplymines.utils;

import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.objects.impl.BasicMine;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WarnUtils {
    public static void checkWarnings(BasicMine mine, long now) {
        long secondsUntilReset = mine.getResetTime() - (now - mine.getLastReset());
        if (secondsUntilReset <= 0) return;

        int seconds = (int) secondsUntilReset;
        if (!mine.getWarnSeconds().contains(seconds)) return;
        if (!mine.getWarnedSeconds().add(seconds)) return; // already warned this second

        // TODO: ADD to config
        String message = "§e" + mine.getName() + " §7resets in §c" + seconds + "s§7!";

        if (mine.isWarnGlobal()) {
            //TODO: Change maybe
            Bukkit.broadcast(Component.text(message));
        } else if (mine.isWarnNear()) {
            warnNearbyPlayers(mine, message);
        }
    }

    private static void warnNearbyPlayers(BasicMine mine, String message) {
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
