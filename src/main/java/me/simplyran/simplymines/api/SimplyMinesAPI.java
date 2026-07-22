package me.simplyran.simplymines.api;

import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import me.simplyran.simplymines.actions.ActionFactory;
import me.simplyran.simplymines.actions.ActionRegistry;
import me.simplyran.simplymines.actions.IAction;
import me.simplyran.simplymines.managers.ConfigManager;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.requirements.mine.IMineRequirement;
import me.simplyran.simplymines.requirements.mine.MineRequirementFactory;
import me.simplyran.simplymines.requirements.mine.MineRequirementRegistry;
import me.simplyran.simplymines.requirements.mine.impl.EfficiencyMineRequirement;
import me.simplyran.simplymines.requirements.mine.impl.PermissionMineRequirement;
import me.simplyran.simplymines.requirements.reset.IResetRequirement;
import me.simplyran.simplymines.requirements.reset.ResetRequirementFactory;
import me.simplyran.simplymines.requirements.reset.ResetRequirementRegistry;
import me.simplyran.simplymines.requirements.reset.impl.PercentResetRequirement;
import me.simplyran.simplymines.requirements.reset.impl.TimeResetRequirement;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class SimplyMinesAPI {

    @Getter private static SimplyMinesAPI INSTANCE;

    private final MineManager mineManager;
    private final SelectionManager selectionManager;
    private final ConfigManager configManager;

    public SimplyMinesAPI(@NotNull MineManager mineManage,
                          @NotNull SelectionManager selectionManager,
                          @NotNull ConfigManager configManager){
        if (INSTANCE == null){
            INSTANCE = this;
        }
        this.mineManager = mineManage;
        this.selectionManager = selectionManager;
        this.configManager = configManager;
    }

    @Nullable
    public BasicMine getMine(@NotNull String mineName){
        return mineManager.getMine(mineName);
    }

    public Collection<BasicMine> getMines(){
        return mineManager.getMines();
    }

    /*
    This will not save mine to disk until next Runnable Save.
     */
    public void addMine(@NotNull BasicMine mine){
        mineManager.addMine(mine);
    }

    /*
    This will also delete the mine file from disk!
     */
    public void deleteMine(@NotNull String mineName){
        mineManager.deleteMine(mineName);
    }

    public Pair<Location, Location> getPlayerSelection(@NotNull UUID playerUUID){
        return selectionManager.getCorners(playerUUID);
    }


    /**
     * Registers a factory for a custom IMineRequirement type so it can be
     * saved to and loaded from disk. Your class must also expose a
     * {@code public static final String NAME} field matching the id you
     * register here — JsonUtils uses it via reflection when serializing.
     */
    public void registerMineRequirement(@NotNull String id, @NotNull MineRequirementFactory factory){
        MineRequirementRegistry.register(id, factory);
    }

    /**
     * Registers a factory for a custom IResetRequirement type so it can be
     * saved to and loaded from disk. Same NAME-field requirement as above.
     */
    public void registerResetRequirement(@NotNull String id, @NotNull ResetRequirementFactory factory){
        ResetRequirementRegistry.register(id, factory);
    }

    /**
     * Registers a factory for a custom IAction type so it can be
     * saved to and loaded from disk.
     */
    public void registerAction(@NotNull String id, @NotNull ActionFactory factory){
        ActionRegistry.register(id, factory);
    }

    public void addAction(@NotNull String mineName,
                          @NotNull String blockName,
                          @NotNull IAction action){
        BasicMine mine = getMine(mineName);
        if (mine != null) mine.addAction(blockName, action);
    }

    public void removeAction(@NotNull String mineName,
                          @NotNull String blockName,
                          @NotNull IAction action){
        BasicMine mine = getMine(mineName);
        if (mine != null) mine.removeAction(blockName, action);
    }


    // ------------------------------------------------------------------
    // Mine Requirements (things a player must satisfy to mine in a mine)
    // ------------------------------------------------------------------

    /**
     * Attaches an arbitrary mine requirement (including your own custom
     * IMineRequirement implementations) to a mine.
     */
    public void addMineRequirement(@NotNull String mineName, @NotNull IMineRequirement requirement){
        BasicMine mine = getMine(mineName);
        if (mine != null) mine.addMineRequirement(requirement);
    }

    public void removeMineRequirement(@NotNull String mineName, @NotNull IMineRequirement requirement){
        BasicMine mine = getMine(mineName);
        if (mine != null) mine.removeMineRequirement(requirement);
    }


    @Nullable
    public List<IMineRequirement> getMineRequirements(@NotNull String mineName){
        BasicMine mine = getMine(mineName);
        return mine != null ? mine.getMineRequirements() : null;
    }

    /**
     * Finds the first mine requirement of a given type attached to a mine, or null
     * if the mine doesn't exist or has none of that type attached.
     */
    @Nullable
    public <T extends IMineRequirement> T getMineRequirement(@NotNull String mineName, @NotNull Class<T> clazz){
        BasicMine mine = getMine(mineName);
        return mine != null ? mine.getMineRequirement(clazz) : null;
    }

    /**
     * Convenience: attaches (or updates, if already present) a minimum tool-efficiency
     * requirement on a mine, enabled by default.
     */
    @Nullable
    public EfficiencyMineRequirement setEfficiencyRequirement(@NotNull String mineName, int efficiencyLevel){
        BasicMine mine = getMine(mineName);
        if (mine == null) return null;

        EfficiencyMineRequirement requirement = mine.getMineRequirement(EfficiencyMineRequirement.class);
        if (requirement == null) {
            requirement = new EfficiencyMineRequirement(configManager, efficiencyLevel);
            requirement.setEnabled(true);
            mine.addMineRequirement(requirement);
        } else {
            requirement.setEfficiencyLevel(efficiencyLevel);
        }
        return requirement;
    }

    /**
     * Convenience: attaches (or updates) a permission-node requirement on a mine,
     * enabled by default.
     */
    @Nullable
    public PermissionMineRequirement setPermissionRequirement(@NotNull String mineName, @NotNull String permission){
        BasicMine mine = getMine(mineName);
        if (mine == null) return null;

        PermissionMineRequirement requirement = mine.getMineRequirement(PermissionMineRequirement.class);
        if (requirement == null) {
            requirement = new PermissionMineRequirement(configManager, permission);
            requirement.setEnabled(true);
            mine.addMineRequirement(requirement);
        } else {
            requirement.setPermission(permission);
        }
        return requirement;
    }

    // ------------------------------------------------------------------
    // Reset Requirements (conditions that trigger a mine reset)
    // ------------------------------------------------------------------

    public void addResetRequirement(@NotNull String mineName, @NotNull IResetRequirement requirement){
        BasicMine mine = getMine(mineName);
        if (mine != null) mine.addResetRequirement(requirement);
    }

    public void removeResetRequirement(@NotNull String mineName, @NotNull IResetRequirement requirement){
        BasicMine mine = getMine(mineName);
        if (mine != null) mine.removeResetRequirement(requirement);
    }

    @Nullable
    public List<IResetRequirement> getResetRequirements(@NotNull String mineName){
        BasicMine mine = getMine(mineName);
        return mine != null ? mine.getResetRequirements() : null;
    }

    @Nullable
    public <T extends IResetRequirement> T getResetRequirement(@NotNull String mineName, @NotNull Class<T> clazz){
        BasicMine mine = getMine(mineName);
        return mine != null ? mine.getResetRequirement(clazz) : null;
    }

    /**
     * Convenience: attaches (or updates) a fixed-interval reset timer on a mine.
     */
    @Nullable
    public TimeResetRequirement setTimeResetRequirement(@NotNull String mineName, int resetTimeSeconds){
        BasicMine mine = getMine(mineName);
        if (mine == null) return null;

        TimeResetRequirement requirement = mine.getResetRequirement(TimeResetRequirement.class);
        if (requirement == null) {
            requirement = new TimeResetRequirement(mine, resetTimeSeconds);
            mine.addResetRequirement(requirement);
        } else {
            requirement.setResetTime(resetTimeSeconds);
        }
        return requirement;
    }

    /**
     * Convenience: attaches (or updates) a percentage-based reset threshold on a mine,
     * enabled by default.
     */
    @Nullable
    public PercentResetRequirement setPercentResetRequirement(@NotNull String mineName, double resetAtPercentage){
        BasicMine mine = getMine(mineName);
        if (mine == null) return null;

        PercentResetRequirement requirement = mine.getResetRequirement(PercentResetRequirement.class);
        if (requirement == null) {
            requirement = new PercentResetRequirement(mine, resetAtPercentage);
            requirement.setEnabled(true);
            mine.addResetRequirement(requirement);
        } else {
            requirement.setResetAtPercentage(resetAtPercentage);
        }
        return requirement;
    }
}