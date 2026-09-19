package me.simplyran.simplymines.objects;

import lombok.Getter;
import lombok.Setter;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import me.simplyran.simplymines.settings.MineSettings;
import me.simplyran.simplymines.utils.ItemUtils;
import me.simplyran.simplymines.utils.Permissions;
import me.simplyran.simplymines.workload.IBlock;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import me.simplyran.simplymines.workload.blocks.*;
import me.simplyran.simplymines.workload.impl.RegionResetWorkload;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class BasicMine {

    private final WorkloadRunnable workloadRunnable;
    @Getter private String name;
    private final Map<String, Double> materials;
    private final Map<String, IBlock> blockCache;

    @Setter
    @Getter private BoxedRegion region;

    @Getter private int blocksBroken;

    @Getter private final List<IResetRequirement> resetRequirements;
    @Getter private final List<IMineRequirement> mineRequirements;
    @Getter private final Map<String, List<IAction>> blocksActions;

    @Getter private final Set<Integer> warnedSeconds = new HashSet<>();

    /** All configurable flags for this mine. */
    @Getter private final MineSettings settings;

    public BasicMine(
            @NotNull String name,
            @NotNull Location corner1,
            @NotNull Location corner2,
            @NotNull Map<String, Double> materials,
            @NotNull WorkloadRunnable workloadRunnable,
            @NotNull MineSettings settings
    ) {
        this.name = name;
        this.region = new BoxedRegion(corner1.getWorld(), corner1, corner2);
        this.materials = new HashMap<>(materials);
        this.workloadRunnable = workloadRunnable;
        this.settings = settings;

        this.resetRequirements = new ArrayList<>();
        this.mineRequirements = new ArrayList<>();
        this.blocksActions = new HashMap<>();
        this.blockCache = new HashMap<>();

        rebuildBlockCache();

        // On creation the next reset will update the mine (if not air blocks), set 1 so it doesn't skip.
        blocksBroken = 1;
    }

    // Keep the same public method names so GUIs and callers don't need to change.

    public boolean isEnabled()                    { return settings.isEnabled(); }
    public void    setEnabled(boolean v)          { settings.setEnabled(v); }

    public boolean isWarnNear()                   { return settings.isWarnNear(); }
    public void    setWarnNear(boolean v)         { settings.setWarnNear(v); }

    public boolean isWarnGlobal()                 { return settings.isWarnGlobal(); }
    public void    setWarnGlobal(boolean v)       { settings.setWarnGlobal(v); }

    public List<Integer> getWarnSeconds()         { return settings.getWarnSeconds(); }

    public boolean isTeleportPlayers()            { return settings.isTeleportPlayers(); }
    public void    setTeleportPlayers(boolean v)  { settings.setTeleportPlayers(v); }

    public int  getWarnDistance()                 { return settings.getWarnDistance(); }
    public void setWarnDistance(int v)            { settings.setWarnDistance(v); }

    public boolean isUsePhysics()                 { return settings.isUsePhysics(); }
    public void    setUsePhysics(boolean v)       { settings.setUsePhysics(v); }

    public boolean isReplaceMode()                { return settings.isReplaceMode(); }
    public void    setReplaceMode(boolean v)      { settings.setReplaceMode(v); }

    public boolean isNormalDropsEnabled()         { return settings.isNormalDropsEnabled(); }
    public void    setNormalDropsEnabled(boolean v) { settings.setNormalDropsEnabled(v); }

    public boolean isFortuneEnabled()             { return settings.isFortuneEnabled(); }
    public void    setFortuneEnabled(boolean v)   { settings.setFortuneEnabled(v); }

    public boolean isAutoPickup()                 { return settings.isAutoPickup(); }
    public void    setAutoPickup(boolean v)       { settings.setAutoPickup(v); }

    public boolean isAutoSmelt()                  { return settings.isAutoSmelt(); }
    public void    setAutoSmelt(boolean v)        { settings.setAutoSmelt(v); }

    public boolean canAutoPickup(@NotNull Player player) {
        return isAutoPickup() && player.hasPermission(Permissions.USE_AUTO_PICKUP);
    }

    public boolean canAutoSmelt(@NotNull Player player) {
        return isAutoSmelt() && player.hasPermission(Permissions.USE_AUTO_SMELT);
    }

    @Nullable
    public Location getTeleportLocation()         { return settings.getTeleportLocation(); }
    public void     setTeleportLocation(@Nullable Location v) { settings.setTeleportLocation(v); }

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

    // ── Reset ─────────────────────────────────────────────────────────────────

    public void reset() {
        reset(false);
    }

    public void reset(boolean force) {
        World world = region.getWorld();
        if (world == null || materials.isEmpty()) return;

        // If replaceMode is disabled and no blocks are broken, skip resetting (unless forced).
        // Note: replaceMode ignores blocksBroken and always resets.
        if (!force && !isReplaceMode() && blocksBroken == 0) return;

        // Evacuate any players standing inside the mine before we bury them.
        if (isTeleportPlayers() && getTeleportLocation() != null) {
            for (Player player : new ArrayList<>(world.getPlayers())) {
                if (region.isInsideRegion(player.getLocation())) {
                    player.teleport(getTeleportLocation());
                }
            }
        }

        rebuildBlockCache();

        boolean onlyReplaceAir = !force && !isReplaceMode();

        workloadRunnable.addWorkload(new RegionResetWorkload(
                workloadRunnable,
                world,
                region.getMinX(), region.getMinY(), region.getMinZ(),
                region.getMaxX(), region.getMaxY(), region.getMaxZ(),
                onlyReplaceAir,
                () -> {
                    IBlock block = blockCache.get(pickMaterial());
                    return block == null ? new Block(Material.AIR) : block;
                }
        ));

        for (IResetRequirement resetRequirement : resetRequirements) {
            resetRequirement.update();
        }

        blocksBroken = 0;
        warnedSeconds.clear();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Rebuilds the block-type cache from the current materials map and physics setting. */
    private void rebuildBlockCache() {
        for (String blockName : materials.keySet()) {
            IBlock block = ItemUtils.getCustomBlock(blockName);
            if (!isUsePhysics()) {
                block = ItemUtils.getNoPhysicsBlock(block);
            }
            blockCache.put(blockName, block);
        }
    }

    /**
     * Picks a material for a block position based on the configured weighted probabilities.
     * Missing percentage is filled by AIR.
     */
    private String pickMaterial() {
        if (materials.size() == 1 && materials.values().iterator().next() >= 1.0) {
            return materials.keySet().iterator().next();
        }

        double x = ThreadLocalRandom.current().nextDouble();
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

    public String getMainMaterial() {
        double max = 0;
        String material = "STONE";
        for (var entry : materials.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                material = entry.getKey();
            }
        }
        return material;
    }

    public double getPercentage(@NotNull String block) {
        Double per = materials.get(block);
        return per == null ? 0 : per;
    }

    public void setPercentage(@NotNull String block, double percentage) {
        if (percentage < 0) percentage = 0;
        if (percentage > 1) percentage = 1;
        addBlock(block, Math.round(percentage * 100.0) / 100.0);
    }

    public double getTotalPercentage() {
        double total = 0;
        for (double i : materials.values()) {
            total += i;
        }
        return total;
    }

    public Set<Map.Entry<String, Double>> getMaterials() {
        return materials.entrySet();
    }

    public void removeBlock(@NotNull String block) {
        materials.remove(block);
        blockCache.remove(block);
    }

    public void addBlock(@NotNull String block, double percent) {
        materials.put(block, percent);
    }

    public void addBlockBroken() {
        blocksBroken += 1;
    }

    public double getPercentageOfMineLeft() {
        long blockCount = region.getBlockCount();
        if (blockCount <= 0) {
            return 0.0;
        }
        return ((double) (blockCount - blocksBroken) / blockCount) * 100.0;
    }

    /**
     * Renames this mine. Saves under the new name first; old record is deleted only on success.
     */
    public void setName(@NotNull String newName, @NotNull MineManager mineManager) {
        if (mineManager.getMine(newName) != null) return;
        String oldName = name;
        this.name = newName;
        mineManager.renameMine(this, oldName);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void addAction(String block, IAction action) {
        blocksActions.computeIfAbsent(block, k -> new ArrayList<>()).add(action);
    }

    public void removeAction(String block, IAction action) {
        List<IAction> actions = blocksActions.get(block);
        if (actions != null) {
            actions.remove(action);
            if (actions.isEmpty()) {
                blocksActions.remove(block);
            }
        }
    }

    public List<IAction> getActions(String block) {
        return blocksActions.getOrDefault(block, Collections.emptyList());
    }

    public Map<String, List<IAction>> getAllActions() {
        return blocksActions;
    }
}
