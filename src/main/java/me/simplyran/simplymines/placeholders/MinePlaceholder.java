package me.simplyran.simplymines.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.TimeUtils;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    // Longest suffix first so "timeleft_formatted"/"timeleft_hms" match before the shorter "timeleft".
    private static final List<String> SUB_PLACEHOLDER_SUFFIXES = List.of(
            "timeleft_formatted", "timeleft_hms", "blocks_broken", "blocks_count",
            "warndistance", "precent_left", "resettime", "timeleft", "enabled", "status"
    );

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

            TimeResetRequirement currentTimeResetRequirement =
                    currentMine.getResetRequirement(TimeResetRequirement.class);
            if (currentTimeResetRequirement == null) {
                return "";
            }

            long secondsLeft = secondsUntilReset(currentTimeResetRequirement);

            if (params.equalsIgnoreCase("currentmine_timeleft")) {
                return String.valueOf(Math.max(0, secondsLeft));
            }

            return TimeUtils.formatMMSS(secondsLeft);
        }

        String sub = null;
        for (String suffix : SUB_PLACEHOLDER_SUFFIXES) {
            if (params.length() > suffix.length() + 1
                    && params.regionMatches(true, params.length() - suffix.length(), suffix, 0, suffix.length())
                    && params.charAt(params.length() - suffix.length() - 1) == '_') {
                sub = suffix;
                break;
            }
        }
        if (sub == null) {
            return null;
        }

        String mineName = params.substring(0, params.length() - sub.length() - 1);
        BasicMine mine = mineManager.getMineIgnoreCase(mineName);
        if (mine == null) {
            return null;
        }

        TimeResetRequirement timeResetRequirement = mine.getResetRequirement(TimeResetRequirement.class);

        return switch (sub) {
            case "timeleft" -> timeResetRequirement == null
                    ? null : String.valueOf(Math.max(0, secondsUntilReset(timeResetRequirement)));
            case "timeleft_formatted" -> timeResetRequirement == null
                    ? null : TimeUtils.formatMMSS(secondsUntilReset(timeResetRequirement));
            case "timeleft_hms" -> timeResetRequirement == null
                    ? null : TimeUtils.formatHMS(secondsUntilReset(timeResetRequirement));
            case "resettime" -> timeResetRequirement == null
                    ? null : String.valueOf(timeResetRequirement.getResetTime());
            case "enabled" -> String.valueOf(mine.isEnabled());
            case "status" -> mine.isEnabled() ? "Enabled" : "Disabled";
            case "warndistance" -> String.valueOf(mine.getWarnDistance());
            case "blocks_broken" -> String.valueOf(mine.getBlocksBroken());
            case "blocks_count" -> String.valueOf(mine.getRegion().getBlockCount());
            case "precent_left" -> String.valueOf(Math.round(mine.getPercentageOfMineLeft() * 10000.0) / 10000.0);
            default -> null;
        };
    }


    @Nullable
    private BasicMine findMineAt(@NotNull Player player) {
        Location location = player.getLocation();
        for (BasicMine mine : mineManager.getMines()) {
            if (mine.isInsideMine(location)) {
                return mine;
            }
        }
        return null;
    }


    private long secondsUntilReset(@NotNull TimeResetRequirement timeResetRequirement) {
        return timeResetRequirement.secondsUntilReset(System.currentTimeMillis() / 1000);
    }
}
