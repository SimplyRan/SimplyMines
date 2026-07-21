package me.simplyran.simplymines.objects;

import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.JsonUtils;
import me.simplyran.simplymines.workload.IBlock;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import me.simplyran.simplymines.workload.blocks.*;
import me.simplyran.simplymines.workload.impl.PlaceableBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class BasicMine{

    private final WorkloadRunnable workloadRunnable;
    @Getter private String name;
    private final Map<String, Double> materials;
    private final Map<String, IBlock> blockCache;

    @Getter @Setter private BoxedRegion region;

    @Getter @Setter private boolean enabled;
    @Getter private int blocksBroken;

    @Getter private final List<IResetRequirement> resetRequirements;
    @Getter private final List<IMineRequirement> mineRequirements;



    //TODO Change
    @Getter private final Set<Integer> warnedSeconds = new HashSet<>();
    @Getter @Setter private boolean warnNear;
    @Getter @Setter private boolean warnGlobal;
    @Getter private final List<Integer> warnSeconds;
    @Getter @Setter private boolean teleportPlayers;
    @Getter @Setter private int warnDistance;
    @Getter @Setter private boolean usePhysics;
    @Getter @Setter private Location teleportLocation;
    @Getter @Setter private boolean replaceMode;
    @Getter @Setter private boolean normalDropsEnabled;

    @Getter @Setter private boolean autoPickup;



    public BasicMine(
            boolean enabled,
            @NotNull String name,
            @NotNull Location corner1,
            @NotNull Location corner2,
            @NotNull Map<String, Double> materials,
            @NotNull WorkloadRunnable workloadRunnable,
            @NotNull List<Integer> warnSeconds,
            boolean warnNear,
            boolean warnGlobal,
            boolean teleportPlayers,
            int warnDistance,
            boolean usePhysics,
            boolean replaceMode,
            boolean normalDropsEnabled,
            boolean autoPickup
    ){
        this.enabled = enabled;
        this.name = name;
        this.region = new BoxedRegion(corner1.getWorld(), corner1, corner2);
        this.materials = new HashMap<>(materials);
        this.workloadRunnable = workloadRunnable;
        this.warnSeconds = new ArrayList<>(warnSeconds);
        this.warnNear = warnNear;
        this.warnGlobal = warnGlobal;
        this.teleportPlayers = teleportPlayers;
        this.warnDistance = warnDistance;
        this.usePhysics = usePhysics;
        this.replaceMode = replaceMode;
        this.normalDropsEnabled = normalDropsEnabled;
        this.autoPickup = autoPickup;

        this.resetRequirements = new ArrayList<>();
        this.mineRequirements = new ArrayList<>();
        this.blockCache = new HashMap<>();


        for (String blockName : materials.keySet()){
            //Put blocks in cache
            IBlock block = ItemUtils.getCustomBlock(blockName);
            if (!usePhysics){
                block = ItemUtils.getNoPhysicsBlock(block);
            }

            blockCache.put(blockName, block);
        }
        //on creating next reset will update the mine (if not air blocks) we set 1 so it doesn't skip.
        blocksBroken = 1;
    }



    public void addResetRequirement(@NotNull IResetRequirement resetRequirement) {
        resetRequirements.add(resetRequirement);
    }

    public void addMineRequirement(@NotNull IMineRequirement mineRequirement) {
        mineRequirements.add(mineRequirement);
    }

    public void removeResetRequirement(@NotNull IResetRequirement resetRequirement) {
        resetRequirements.remove(resetRequirement);
    }

    public void removeMineRequirement(@NotNull IMineRequirement mineRequirement) {
        mineRequirements.remove(mineRequirement);
    }

    /** Finds the first reset requirement of a given concrete type, or null if none is attached. */
    public <T extends IResetRequirement> T getResetRequirement(Class<T> clazz) {
        for (IResetRequirement requirement : resetRequirements) {
            if (clazz.isInstance(requirement)) return clazz.cast(requirement);
        }
        return null;
    }

    /** Finds the first mine requirement of a given concrete type, or null if none is attached. */
    public <T extends IMineRequirement> T getMineRequirement(Class<T> clazz) {
        for (IMineRequirement requirement : mineRequirements) {
            if (clazz.isInstance(requirement)) return clazz.cast(requirement);
        }
        return null;
    }

    public void reset(){
        reset(false);
    }


    public void reset(boolean force) {
        World world = region.getWorld();
        if (world == null || materials.isEmpty()) return;

        // If replaceMode is disabled and no blocks are broken, skip resetting (unless forced)
        // Note: If replaceMode is enabled, we ignore blocksBroken and always reset!
        if (!force && !replaceMode && blocksBroken == 0) return;

        // Evacuate any players standing inside the mine before we bury them
        if (teleportPlayers && teleportLocation != null) {
            for (Player player : new ArrayList<>(world.getPlayers())) {
                if (region.isInsideRegion(player.getLocation())) {
                    player.teleport(teleportLocation);
                }
            }
        }

        for (String blockName : materials.keySet()){
            //Put blocks in cache
            IBlock block = ItemUtils.getCustomBlock(blockName);
            if (!usePhysics){
                block = ItemUtils.getNoPhysicsBlock(block);
            }

            blockCache.put(blockName, block);
        }

        // Queue a placement task for every block position in the region
        for (int x = region.getMinX(); x <= region.getMaxX(); x++) {
            for (int y = region.getMinY(); y <= region.getMaxY(); y++) {
                for (int z = region.getMinZ(); z <= region.getMaxZ(); z++) {

                    String material = pickMaterial();
                    IBlock block = blockCache.get(material);
                    if (block == null) block = new Block(Material.AIR);
                    Location loc = new Location(world, x, y, z);

                    // If replaceMode is false, we ONLY change air blocks.
                    // Therefore, if it's not forced, replaceMode is false, and the block is NOT empty -> skip it.
                    if (!force && !replaceMode && !loc.getBlock().isEmpty()) {
                        continue;
                    }

                    workloadRunnable.addWorkload(
                            new PlaceableBlock(loc, block)
                    );
                }
            }
        }

        for (IResetRequirement resetRequirement : resetRequirements){
            resetRequirement.update();
        }

        blocksBroken = 0;
        warnedSeconds.clear();
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

    public double getPercentage(@NotNull String block){
        Double per = materials.get(block);
        return per == null ? 0 : per;
    }


    public void setPercentage(@NotNull String block,
                              double percentage){
        if (percentage < 0) percentage = 0;
        if (percentage > 1) percentage = 1;
        addBlock(block, Math.round(percentage * 100.0) / 100.0);
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
        blockCache.remove(block);
    }

    public void addBlock(@NotNull String block, double precent){
        materials.put(block, precent);
    }


    public void addBlockBroken(){
        blocksBroken += 1;
    }

    public double getPercentageOfMineLeft() {
        long blockCount = region.getBlockCount();
        if (blockCount <= 0) {
            return 0.0;
        }

        return ((double) (blockCount - blocksBroken) / blockCount) * 100.0;
    }

    /*
    This Makes sure when you change name it delete the old file and create new one.
     */
    public void setName(@NotNull String newName,
                        @NotNull MineManager mineManager,
                        @NotNull SimplyMines plugin){
        //Mine with this name already exist!
        if (mineManager.getMine(newName) != null) return;
        String oldName = name;
        this.name = newName;
        saveAndDelete(plugin, oldName);
        mineManager.deleteMine(oldName);
        mineManager.addMine(this);
    }

    private void saveAndDelete(SimplyMines plugin, String oldName){
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (JsonUtils.saveMine(plugin, this)) {
                JsonUtils.deleteMine(plugin, oldName);
            }
        });
    }



}