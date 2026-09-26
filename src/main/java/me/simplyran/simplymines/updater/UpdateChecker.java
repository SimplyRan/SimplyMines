package me.simplyran.simplymines.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.simplyran.simplymines.SimplyMines;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private static final String PROJECT_SLUG = "simplymines";
    private static final String VERSIONS_URL = "https://api.modrinth.com/v2/project/" + PROJECT_SLUG + "/version";
    public static final String PROJECT_PAGE_URL = "https://modrinth.com/plugin/" + PROJECT_SLUG;

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final SimplyMines plugin;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final AtomicReference<UpdateCheckResult> lastResult = new AtomicReference<>();
    private final AtomicBoolean checkInProgress = new AtomicBoolean(false);

    public UpdateChecker(@NotNull SimplyMines plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<UpdateCheckResult> checkAsync() {
        if (!checkInProgress.compareAndSet(false, true)) {
            return CompletableFuture.completedFuture(lastResult.get());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VERSIONS_URL))
                .header("User-Agent", "SimplyMines/" + plugin.getDescription().getVersion())
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseResponse)
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Could not check for SimplyMines updates: " + ex.getMessage());
                    return lastResult.get();
                })
                .whenComplete((result, throwable) -> {
                    checkInProgress.set(false);
                    if (result != null) {
                        lastResult.set(result);
                        if (result.updateAvailable()) {
                            announceUpdate(result);
                        }
                    }
                });
    }

    private void announceUpdate(@NotNull UpdateCheckResult result) {
        String banner = """
                <newline>\
                <#ffd166>  A new SimplyMines update is available
                <dark_gray>  ───────────────────────────────────────
                <#8b9bb4>  Running:  <white><current>
                <#8b9bb4>  Latest:   <#7bd88f><latest>
                <#8b9bb4>  Download: <#ffd166><underlined><url></underlined>
                <newline>""";

        Bukkit.getConsoleSender().sendMessage(MINI_MESSAGE.deserialize(banner,
                Placeholder.unparsed("current", result.currentVersion()),
                Placeholder.unparsed("latest", result.latestVersion()),
                Placeholder.unparsed("url", PROJECT_PAGE_URL)));
    }

    public Optional<UpdateCheckResult> getLastResult() {
        return Optional.ofNullable(lastResult.get());
    }

    private UpdateCheckResult parseResponse(HttpResponse<String> response) {
        String current = plugin.getDescription().getVersion();

        if (response.statusCode() != 200) {
            plugin.getLogger().warning("Modrinth API returned HTTP " + response.statusCode() + " while checking for updates.");
            return new UpdateCheckResult(current, null, false, true);
        }

        try {
            JsonArray versions = JsonParser.parseString(response.body()).getAsJsonArray();

            JsonObject latestEntry = null;
            String latestDate = null;

            for (JsonElement element : versions) {
                JsonObject entry = element.getAsJsonObject();
                String published = entry.has("date_published") ? entry.get("date_published").getAsString() : null;
                if (published != null && (latestDate == null || published.compareTo(latestDate) > 0)) {
                    latestDate = published;
                    latestEntry = entry;
                }
            }

            if (latestEntry == null || !latestEntry.has("version_number")) {
                return new UpdateCheckResult(current, null, false, true);
            }

            String latest = latestEntry.get("version_number").getAsString();
            return new UpdateCheckResult(current, latest, isNewer(latest, current), false);
        } catch (Exception e) {
            plugin.getLogger().warning("Unexpected response from Modrinth API while checking for updates: " + e.getMessage());
            return new UpdateCheckResult(current, null, false, true);
        }
    }

    private static final Pattern VERSION_SEGMENT = Pattern.compile("\\d+");

    static boolean isNewer(@NotNull String latest, @NotNull String current) {
        if (latest.equals(current)) return false;

        int[] latestSegments = extractSegments(latest);
        int[] currentSegments = extractSegments(current);

        if (latestSegments.length == 0 || currentSegments.length == 0) {
            return !latest.equals(current);
        }

        int length = Math.max(latestSegments.length, currentSegments.length);
        for (int i = 0; i < length; i++) {
            int latestPart = i < latestSegments.length ? latestSegments[i] : 0;
            int currentPart = i < currentSegments.length ? currentSegments[i] : 0;
            if (latestPart != currentPart) {
                return latestPart > currentPart;
            }
        }
        return false;
    }

    private static int[] extractSegments(@NotNull String version) {
        Matcher matcher = VERSION_SEGMENT.matcher(version);
        List<Integer> segments = new ArrayList<>();
        while (matcher.find()) {
            try {
                segments.add(Integer.parseInt(matcher.group()));
            } catch (NumberFormatException ignored) {
            }
        }
        return segments.stream().mapToInt(Integer::intValue).toArray();
    }
}
