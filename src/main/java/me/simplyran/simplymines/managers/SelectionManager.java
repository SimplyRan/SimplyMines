package me.simplyran.simplymines.managers;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SelectionManager {


    private final Map<UUID, Pair<Location, Location>> cornersMap;
    private final Set<UUID> toolDisabled;

    public SelectionManager(){
        cornersMap = new HashMap<>();
        toolDisabled = new HashSet<>();
    }

    @Nullable
    public Pair<Location, Location> getCorners(UUID uuid){
        return cornersMap.get(uuid);
    }

    public void setCorener(@NotNull UUID uuid,
                           Location location,
                           int position) {

        cornersMap.putIfAbsent(uuid, Pair.of(null, null));

        Pair<Location, Location> pair = cornersMap.get(uuid);

        if (position == 1) {
            cornersMap.put(uuid, Pair.of(location, pair.right()));
        } else if (position == 2) {
            cornersMap.put(uuid, Pair.of(pair.left(), location));
        } else {
            throw new RuntimeException("Corner Position not 1 or 2!");
        }
    }

    public void toggleTool(UUID player){
        if (toolDisabled.contains(player)) toolDisabled.remove(player);
        else toolDisabled.add(player);
    }

    public boolean isToolDisabled(UUID player){
        return toolDisabled.contains(player);
    }

    public void deleteCorners(UUID uuid){
        cornersMap.remove(uuid);
    }

}
