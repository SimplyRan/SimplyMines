package me.simplyran.simplymines.factories;

import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.settings.MineSettings;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MineFactory {

    public static BasicMine createDefaultMin(@NotNull String mineName,
                                             @NotNull Pair<Location, Location> corners,
                                             @NotNull WorkloadRunnable workloadRunnable) {

        if (corners.first() == null || corners.second() == null) {
            throw new RuntimeException("Cannot create new mine with null corners!");
        }

        return new BasicMine(
                mineName,
                corners.first(),
                corners.second(),
                Map.of(),
                workloadRunnable,
                MineSettings.createDefault()
        );
    }
}
