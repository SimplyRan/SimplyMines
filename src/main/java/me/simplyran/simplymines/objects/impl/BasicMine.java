package me.simplyran.simplymines.objects.impl;

import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.objects.BoxedRegion;
import me.simplyran.simplymines.objects.IMine;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.workload.IBlock;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import me.simplyran.simplymines.workload.blocks.Block;
import me.simplyran.simplymines.workload.blocks.NoPhysicsBlock;
import me.simplyran.simplymines.workload.impl.PlaceableBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class BasicMine implements IMine {

    private final WorkloadRunnable workloadRunnable;
    private final String name;
    private final BoxedRegion region;
    private final Map<String, Double> materials;
    private final Map<String, IBlock> blockCahce;
    private long lastReset;
    private int resetTime;
    private boolean enabled;

    //Settings:
    @Getter private final Set<Integer> warnedSeconds = new HashSet<>();
    @Getter @Setter private boolean warnNear;
    @Getter @Setter private boolean warnGlobal;
    @Getter private final List<Integer> warnSeconds;
    @Getter @Setter private boolean teleportPlayers;
    @Getter @Setter private int warnDistance;
    @Getter @Setter private boolean usePhysics;


    public BasicMine(
            boolean enabled,
            @NotNull String name,
            int resetTime,
            @NotNull Location corner1,
            @NotNull Location corner2,
            @NotNull Map<String, Double> materials,
            @NotNull WorkloadRunnable workloadRunnable,
            @NotNull List<Integer> warnSeconds,
            boolean warnNear,
            boolean warnGlobal,
            boolean teleportPlayers,
            int warnDistance,
            boolean usePhysics
    ){
        this.enabled = enabled;
        this.name = name;
        this.region = new BoxedRegion(corner1.getWorld(), corner1, corner2);
        this.materials = new HashMap<>(materials);
        this.resetTime = resetTime;
        this.workloadRunnable = workloadRunnable;
        this.warnSeconds = new ArrayList<>(warnSeconds);
        this.warnNear = warnNear;
        this.warnGlobal = warnGlobal;
        this.teleportPlayers = teleportPlayers;
        this.warnDistance = warnDistance;
        this.usePhysics = usePhysics;


        blockCahce = new HashMap<>();
        for (String blockName : materials.keySet()){
            //Put blocks in cache
            blockCahce.put(blockName, ItemUtils.getCustomBlock(blockName));
        }

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
        if (teleportPlayers) {
            for (Player player : world.getPlayers()) {
                Location loc = player.getLocation();
                if (region.isInsideRegion(loc)) {
                    player.teleport(loc.clone().add(0, (region.getMaxY() - loc.getBlockY()) + 1, 0));
                }
            }
        }

        for (String blockName : materials.keySet()){
            //Put blocks in cache
            IBlock block = ItemUtils.getCustomBlock(blockName);
            if (block instanceof Block mcBlock && !usePhysics)
                block = new NoPhysicsBlock(mcBlock.getMaterial());

            blockCahce.put(blockName, block);
        }

        // Queue a placement task for every block position in the region
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {
                    String material = pickMaterial();
                    IBlock block = blockCahce.get(material);
                    if (block == null) block = new Block(Material.AIR);

                    workloadRunnable.addWorkload(
                            new PlaceableBlock(world.getUID(), x, y,z, block)
                    );
                }
            }
        }

        lastReset = System.currentTimeMillis();
        warnedSeconds.clear();
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
     * Picks a material for a block position based on the configured weighted probabilities. (Missing % is AIR)
     */
    private String pickMaterial() {
        if (materials.size() == 1 && materials.values().iterator().next() >= 1.0) {
            return materials.keySet().iterator().next();
        }

        double x = Math.random();
        double cumulativeSum = 0.0d;
        for (Map.Entry<String, Double> entry : materials.entrySet()) {
            cumulativeSum += entry.getValue();
            if (x < cumulativeSum) {
                return entry.getKey();
            }
        }


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

    public double getPercentage(@NotNull String block){
        Double per = materials.get(block);
        return per == null ? 0 : per;
    }


    public void setPercentage(@NotNull String block,
                              double percentage){
        if (percentage < 0) percentage = 0;
        if (percentage > 1) percentage = 1;
        materials.put(block, Math.round(percentage * 100.0) / 100.0);
    }

    public double getTotalPercentage(){
        double total = 0;
        for (double i : materials.values()){
            total += i;
        }
        return total;
    }

    public Set<Map.Entry<String, Double>> getMaterials(){
        return materials.entrySet();
    }

    public void removeBlock(@NotNull String block){
        materials.remove(block);
        blockCahce.remove(block);
    }

}