package me.simplyran.simplymines.objects.impl;

import lombok.Getter;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.objects.IMine;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public class BasicMine implements IMine {

    private final WorkloadRunnable workloadRunnable;
    private final String name;
    private final BoxedRegion region;
    @Getter private final Map<String, Double> materials;
    private long lastReset;
    private int resetTime;
    private boolean enabled = true;


    public BasicMine(
            boolean enabled,
            @NotNull String name,
            int resetTime,
            @NotNull Location corner1,
            @NotNull Location corner2,
            @NotNull Map<String, Double> materials,
            @NotNull WorkloadRunnable workloadRunnable){
        this.enabled = enabled;
        this.name = name;
        this.region = new BoxedRegion(corner1.getWorld(), corner1, corner2);
        this.materials = new HashMap<>(materials);
        this.resetTime = resetTime;
        this.workloadRunnable = workloadRunnable;
    }




    @Override
    public BoxedRegion getRegion() {
        return region;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getResetTime() {
        return resetTime;
    }

    @Override
    public long getLastReset() {
        return lastReset;
    }

    @Override
    public void setLastReset(long lastReset) {
        this.lastReset = lastReset;
    }

    @Override
    public void reset() {
        World world = region.getWorld();
        if (world == null || materials.isEmpty()) return;

        // Don't reset a chunk nobody can see (assumes BoxedRegion exposes a center/loaded check;
        // adjust to your actual API, e.g. region.isChunkLoaded())
        // if (!world.isChunkLoaded(region.getCenterX() >> 4, region.getCenterZ() >> 4)) return;

        // Evacuate any players standing inside the mine before we bury them
        for (Player player : world.getPlayers()) {
            Location loc = player.getLocation();
            if (region.isInsideRegion(loc)) {
                player.teleport(loc.clone().add(0, (region.getMaxY() - loc.getBlockY()) + 1, 0));
            }
        }

        // Queue a placement task for every block position in the region
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    String material = pickMaterial();

                    workloadRunnable.addWorkload(
                            ItemUtils.getCustomWorkload(material, new Location(world, x, y,z))
                    );
                }
            }
        }

        lastReset = System.currentTimeMillis();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Picks a material for a block position based on the configured weighted probabilities.
     * If there's only one material configured, skips the RNG entirely.
     */
    private String pickMaterial() {
        if (materials.size() == 1) {
            return materials.keySet().iterator().next();
        }

        double x = Math.random();
        double cumulativeSum = 0.0d;
        for (Map.Entry<String, Double> entry : materials.entrySet()) {
            cumulativeSum += entry.getValue();
            if (cumulativeSum >= x) {
                return entry.getKey();
            }
        }

        // Fallback in case probabilities don't sum to exactly 1.0 (floating point drift,
        // or a misconfigured mine) — avoids leaving the block untouched.
        return "AIR";
    }


    @Override
    public boolean isInsideMine(Location location) {
        return region.isInsideRegion(location);
    }


    public String getMainMaterial(){
        double max = 0;
        String material = "STONE";
        for (var entry : materials.entrySet()){
            if (entry.getValue() > max){
                max = entry.getValue();
                material = entry.getKey();
            }
        }
        return material;
    }

    @Override
    public void setResetTime(int resetTime){
        this.resetTime = resetTime;
    }

}