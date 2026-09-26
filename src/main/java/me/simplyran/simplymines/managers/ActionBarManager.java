package me.simplyran.simplymines.managers;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.factories.ConfigFactory;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.ConfigData;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import me.simplyran.simplymines.utils.MessageUtils;
import me.simplyran.simplymines.utils.MineChunkIndex;
import me.simplyran.simplymines.utils.Permissions;
import me.simplyran.simplymines.utils.TimeUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Shows an action bar (mine name, percentage left, time until reset, ...) to players standing in a mine.
 * <p>
 * Cost per run is kept flat on purpose:
 * <ul>
 *   <li>Finding a player's mine goes through a {@link MineChunkIndex}: no scan over all mines and no
 *       {@code Location} allocation per player. The index rebuilds itself only when mines, their
 *       enabled state or the configured distance change.</li>
 *   <li>Placeholder values and the formatted message are computed once per <i>mine</i> per run and
 *       shared by every player in it. Only messages that use PlaceholderAPI ({@code %...%}) are
 *       formatted per player, since their output depends on the player.</li>
 *   <li>Nothing is sent to players outside mines, and a single empty bar is sent when someone leaves.</li>
 * </ul>
 */
public class ActionBarManager implements Runnable {

    private static final int MAX_DISTANCE = 128;
    private static final int MAX_UPDATE_TICKS = 1200;

    private final SimplyMines plugin;
    private final MineManager mineManager;
    private final MineChunkIndex index = new MineChunkIndex();

    // Declared as wildcards so a hand-edited value of the wrong type can be handled instead of throwing.
    private final ConfigData<?> enabledConfig = ConfigFactory.newConfigData("actionbar.enabled", true);
    private final ConfigData<?> updateTicksConfig = ConfigFactory.newConfigData("actionbar.update_ticks", 20);
    private final ConfigData<?> distanceConfig = ConfigFactory.newConfigData("actionbar.distance", 0);

    // Validated copies of the config values, refreshed on load and on every reload.
    private boolean enabled;
    private int updateTicks;
    private int distance;

    private BukkitTask task;
    private long scheduledTicks;

    private final ConfigData<String> messageTimed = ConfigFactory.newConfigData(
            "messages.actionbar-timed",
            "<#ffd166><mine> <dark_gray>| <#8b9bb4>Left <#7bd88f><percent_left>% <dark_gray>(<white><blocks_left><dark_gray>/<white><blocks_total><dark_gray>) | <#8b9bb4>Reset <#ef6f6c><time_left>");
    private final ConfigData<String> messageUntimed = ConfigFactory.newConfigData(
            "messages.actionbar-untimed",
            "<#ffd166><mine> <dark_gray>| <#8b9bb4>Left <#7bd88f><percent_left>% <dark_gray>(<white><blocks_left><dark_gray>/<white><blocks_total><dark_gray>)");

    /** Players who currently see the bar, and the ones seen during this run (swapped after each run). */
    private Set<UUID> shown = new HashSet<>();
    private Set<UUID> seen = new HashSet<>();
    private boolean loggedFailure = false;

    /** {@link MessageUtils#hasPlaceholderApi} result for the last message text, so it isn't re-scanned per player. */
    /** Whether each message text uses PlaceholderAPI; the timed and untimed texts alternate so one slot would thrash. */
    private final Map<String, Boolean> papiByText = new HashMap<>();

    /** Per-run caches, one entry per mine that has at least one player in it. */
    private final Map<BasicMine, View> views = new IdentityHashMap<>();

    /** What every player in one mine gets to see this run. */
    private static final class View {
        final String[] pairs;
        final ConfigData<String> message;
        Component shared;

        View(String[] pairs, ConfigData<String> message) {
            this.pairs = pairs;
            this.message = message;
        }
    }

    public ActionBarManager(@NotNull SimplyMines plugin,
                            @NotNull MineManager mineManager,
                            @NotNull ConfigManager configManager) {
        this.plugin = plugin;
        this.mineManager = mineManager;
        configManager.register(enabledConfig);
        configManager.register(updateTicksConfig);
        configManager.register(distanceConfig);
        configManager.registerLang(messageTimed);
        configManager.registerLang(messageUntimed);
        configManager.addReloadListener(this::reload);
        readSettings();
    }

    public void start() {
        stop();
        schedule(5 * 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Called after config.yml and lang were reloaded. Re-validates the settings and restarts the
     * timer if the update interval changed. The index notices a changed distance (and reloaded
     * mines) on its own during the next run, and the messages are picked up as they are formatted.
     */
    private void reload() {
        readSettings();
        if (task != null && scheduledTicks != updateTicks) {
            stop();
            schedule(updateTicks);
        }
    }

    private void schedule(long delay) {
        scheduledTicks = updateTicks;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this, delay, updateTicks);
    }

    private void readSettings() {
        enabled = readBoolean(enabledConfig, true);
        updateTicks = readInt(updateTicksConfig, 1, MAX_UPDATE_TICKS, 20);
        distance = readInt(distanceConfig, 0, MAX_DISTANCE, 0);
    }

    private boolean readBoolean(ConfigData<?> data, boolean fallback) {
        if (data.getValue() instanceof Boolean value) return value;
        plugin.getLogger().warning("Config '" + data.getPath() + "' must be true or false, using " + fallback + ".");
        return fallback;
    }

    private int readInt(ConfigData<?> data, int min, int max, int fallback) {
        if (!(data.getValue() instanceof Number number)) {
            plugin.getLogger().warning("Config '" + data.getPath() + "' must be a number, using " + fallback + ".");
            return fallback;
        }
        int value = number.intValue();
        int clamped = Math.max(min, Math.min(max, value));
        if (clamped != value) {
            plugin.getLogger().warning("Config '" + data.getPath() + "' must be between " + min + " and "
                    + max + ", using " + clamped + ".");
        }
        return clamped;
    }

    @Override
    public void run() {
        if (!enabled) {
            clearAll();
            return;
        }

        var mines = mineManager.getMines();
        if (index.isStale(mines, distance)) {
            index.rebuild(mines, distance);
        }
        if (index.isEmpty()) {
            clearAll();
            return;
        }

        boolean papi = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
        long now = System.currentTimeMillis() / 1000;

        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                BasicMine mine = index.find(player.getWorld(), player.getX(), player.getY(), player.getZ());
                if (mine == null) continue;
                if (!player.hasPermission(Permissions.SEE_ACTIONBAR)) continue;

                try {
                    View view = views.get(mine);
                    if (view == null) {
                        view = buildView(mine, now);
                        views.put(mine, view);
                    }

                    player.sendActionBar(component(view, player, papi));
                    seen.add(player.getUniqueId());
                } catch (RuntimeException e) {
                    // A broken placeholder must not stop the bar for everyone else.
                    if (!loggedFailure) {
                        loggedFailure = true;
                        Bukkit.getLogger().warning("Could not build the action bar for " + player.getName() + ": " + e);
                    }
                }
            }
        } finally {
            views.clear();
        }

        // Players who had the bar last run but aren't in a mine anymore: remove it right away
        // instead of letting it linger for the rest of its display time.
        for (UUID uuid : shown) {
            if (seen.contains(uuid)) continue;
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.sendActionBar(Component.empty());
        }

        Set<UUID> swap = shown;
        shown = seen;
        seen = swap;
        seen.clear();
    }

    private Component component(View view, Player player, boolean papi) {
        // Only PlaceholderAPI output is player specific, everything else is identical within a mine.
        if (papi && hasPlaceholderApi(view.message.getValue())) {
            return MessageUtils.format(player, view.message, view.pairs);
        }
        if (view.shared == null) {
            view.shared = MessageUtils.format(view.message, view.pairs);
        }
        return view.shared;
    }

    private boolean hasPlaceholderApi(String text) {
        if (papiByText.size() > 16) papiByText.clear();
        return papiByText.computeIfAbsent(text, MessageUtils::hasPlaceholderApi);
    }

    private View buildView(BasicMine mine, long now) {
        long total = mine.getRegion().getBlockCount();
        long left = Math.max(0, total - mine.getBlocksBroken());

        TimeResetRequirement timer = mine.getResetRequirement(TimeResetRequirement.class);
        if (timer == null) {
            return new View(new String[]{
                    "mine", mine.getName(),
                    "percent_left", percent(left, total),
                    "blocks_left", String.valueOf(left),
                    "blocks_broken", String.valueOf(mine.getBlocksBroken()),
                    "blocks_total", String.valueOf(total)
            }, messageUntimed);
        }

        return new View(new String[]{
                "mine", mine.getName(),
                "percent_left", percent(left, total),
                "blocks_left", String.valueOf(left),
                "blocks_broken", String.valueOf(mine.getBlocksBroken()),
                "blocks_total", String.valueOf(total),
                "time_left", TimeUtils.formatHMS(timer.secondsUntilReset(now)),
                "reset_time", TimeUtils.formatHMS(timer.getResetTime())
        }, messageTimed);
    }

    /** {@code left / total} as a percentage with one decimal, e.g. "87.3". */
    private static String percent(long left, long total) {
        if (total <= 0) return "0.0";
        long tenths = Math.round(left * 1000.0 / total);
        return (tenths / 10) + "." + (tenths % 10);
    }

    /** Removes the bar from everyone who still has it, then forgets them. */
    private void clearAll() {
        if (shown.isEmpty()) return;
        for (UUID uuid : shown) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) player.sendActionBar(Component.empty());
        }
        shown.clear();
    }
}
