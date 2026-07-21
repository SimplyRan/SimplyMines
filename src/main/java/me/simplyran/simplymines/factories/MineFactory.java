package me.simplyran.simplymines.factories;

import it.unimi.dsi.fastutil.Pair;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class MineFactory {

    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_WARN_NEAR = false;
    private static final boolean DEFAULT_WARN_GLOBAL = false;
    private static final int DEFAULT_WARN_DISTANCE = 1;
    private static final boolean DEFAULT_TELEPORT_PLAYERS = false;
    private static final boolean DEFAULT_USE_PHYSICS = false;
    private static final boolean DEFAULT_REPLACE_MODE = true;
    private static final boolean DEFAULT_NORMAL_DROPS_ENABLED = true;
    private static final boolean DEFAULT_AUTO_PICKUP = false;

    public static BasicMine createDefaultMin(@NotNull String mineName,
                                             @NotNull Pair<Location, Location> corners,
                                             @NotNull WorkloadRunnable workloadRunnable){

        if (corners.first() == null || corners.second() == null){
            throw new RuntimeException("Cannot create new mine with null corners!");
        }

        return new BasicMine(
                DEFAULT_ENABLED,
                mineName,
                corners.first(),
                corners.second(),
                Map.of(),
                workloadRunnable,
                List.of(),
                DEFAULT_WARN_NEAR,
                DEFAULT_WARN_GLOBAL,
                DEFAULT_TELEPORT_PLAYERS,
                DEFAULT_WARN_DISTANCE,
                DEFAULT_USE_PHYSICS,
                DEFAULT_REPLACE_MODE,
                DEFAULT_NORMAL_DROPS_ENABLED,
                DEFAULT_AUTO_PICKUP
        );

    }
}
