package me.simplyran.simplymines.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ------------------------------------------------------------------
 * PLACEHOLDERS
 * ------------------------------------------------------------------
 * Per-mine (replace <mine> with the exact mine name, case-insensitive):
 *   %simplymines_<mine>_timeleft%            -> raw seconds until reset (e.g. "142")
 *   %simplymines_<mine>_timeleft_formatted%   -> "mm:ss" (e.g. "02:22")
 *   %simplymines_<mine>_timeleft_hms%         -> "h:mm:ss" for long timers (e.g. "1:02:22")
 *   %simplymines_<mine>_resettime%            -> configured reset interval in seconds
 *   %simplymines_<mine>_enabled%              -> "true" / "false"
 *   %simplymines_<mine>_status%               -> "Enabled" / "Disabled" (nice display text)
 *   %simplymines_<mine>_warndistance%         -> configured warn distance
 * Player-relative (no mine name needed, based on the player's current location):
 *   %simplymines_currentmine%                 -> name of the mine the player is standing in, or "None"
 *   %simplymines_currentmine_timeleft%        -> seconds until that mine resets, or "" if not in one
 *   %simplymines_currentmine_timeleft_formatted% -> "mm:ss" for the mine the player is standing in
 * Global:
 *   %simplymines_count%                       -> total number of mines
 *   %simplymines_count_enabled%                -> number of currently enabled mines
 * ------------------------------------------------------------------
 */
public class MinePlaceholder extends PlaceholderExpansion {

    private final MineManager mineManager;

    public MinePlaceholder(@NotNull MineManager mineManager) {
        this.mineManager = mineManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "simplymines";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SimplyRan";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {

        if (params.equalsIgnoreCase("count")) {
            return String.valueOf(mineManager.getMines().size());
        }

        if (params.equalsIgnoreCase("count_enabled")) {
            long count = mineManager.getMines().stream().filter(BasicMine::isEnabled).count();
            return String.valueOf(count);
        }

        if (params.equalsIgnoreCase("currentmine")
                || params.equalsIgnoreCase("currentmine_timeleft")
                || params.equalsIgnoreCase("currentmine_timeleft_formatted")) {

            if (!(offlinePlayer instanceof Player player) || !player.isOnline()) {
                return "";
            }

            BasicMine currentMine = findMineAt(player);

            if (params.equalsIgnoreCase("currentmine")) {
                return currentMine == null ? "None" : currentMine.getName();
            }

            if (currentMine == null) {
                return "";
            }

            long secondsLeft = secondsUntilReset(currentMine);

            if (params.equalsIgnoreCase("currentmine_timeleft")) {
                return String.valueOf(Math.max(0, secondsLeft));
            }

            return formatMMSS(secondsLeft);
        }

        String[] parts = params.split("_", 2);
        if (parts.length != 2) {
            return null; // unknown placeholder
        }

        String mineName = parts[0];
        String sub = parts[1];

        BasicMine mine = findMineIgnoreCase(mineName);
        if (mine == null) {
            return null;
        }

        return switch (sub.toLowerCase()) {
            case "timeleft" -> String.valueOf(Math.max(0, secondsUntilReset(mine)));
            case "timeleft_formatted" -> formatMMSS(secondsUntilReset(mine));
            case "timeleft_hms" -> formatHMS(secondsUntilReset(mine));
            case "resettime" -> String.valueOf(mine.getResetTime());
            case "enabled" -> String.valueOf(mine.isEnabled());
            case "status" -> mine.isEnabled() ? "Enabled" : "Disabled";
            case "warndistance" -> String.valueOf(mine.getWarnDistance());
            case "blocks_broken" -> String.valueOf(mine.getBlocksBroken());
            case "blocks_count" -> String.valueOf(mine.getRegion().getBlockCount());
            case "precent_left" -> String.valueOf(Math.round(getPercentageLeft(mine.getRegion().getBlockCount(), mine.getBlocksBroken()) * 100));
            default -> null;
        };
    }


    @Nullable
    private BasicMine findMineIgnoreCase(@NotNull String name) {
        for (BasicMine mine : mineManager.getMines()) {
            if (mine.getName().equalsIgnoreCase(name)) {
                return mine;
            }
        }
        return null;
    }

    @Nullable
    private BasicMine findMineAt(@NotNull Player player) {
        for (BasicMine mine : mineManager.getMines()) {
            if (mine.isInsideMine(player.getLocation())) {
                return mine;
            }
        }
        return null;
    }

    public double getPercentageLeft(long blockCount, long blocksBroken) {
        if (blockCount <= 0) {
            return 0.0;
        }

        return ((double) (blockCount - blocksBroken) / blockCount) * 100.0;
    }

    private long secondsUntilReset(@NotNull BasicMine mine) {
        long now = System.currentTimeMillis() / 1000;
        return mine.getResetTime() - (now - mine.getLastReset());
    }

    private String formatMMSS(long totalSeconds) {
        long clamped = Math.max(0, totalSeconds);
        long minutes = clamped / 60;
        long seconds = clamped % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String formatHMS(long totalSeconds) {
        long clamped = Math.max(0, totalSeconds);
        long hours = clamped / 3600;
        long minutes = (clamped % 3600) / 60;
        long seconds = clamped % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}

