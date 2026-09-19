package me.simplyran.simplymines.settings;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * All configurable flags for a mine in one place.
 * This is the single source of truth for default values — both
 * {@link me.simplyran.simplymines.factories.MineFactory} and
 * {@link me.simplyran.simplymines.database.MineSerializer} read from the
 * DEFAULT_* constants here, so new mines and reloaded mines always start
 * from the same baseline.
 */
@Getter
@Setter
public class MineSettings {

    public static final boolean DEFAULT_ENABLED              = true;
    public static final boolean DEFAULT_WARN_NEAR            = false;
    public static final boolean DEFAULT_WARN_GLOBAL          = false;
    public static final List<Integer> DEFAULT_WARN_SECONDS   = List.of(30, 15, 5, 3, 2, 1);
    public static final boolean DEFAULT_TELEPORT_PLAYERS     = false;
    public static final int     DEFAULT_WARN_DISTANCE        = 50;
    public static final boolean DEFAULT_USE_PHYSICS          = false;
    public static final boolean DEFAULT_REPLACE_MODE         = true;
    public static final boolean DEFAULT_NORMAL_DROPS_ENABLED = true;
    public static final boolean DEFAULT_FORTUNE_ENABLED      = false;
    public static final boolean DEFAULT_AUTO_PICKUP          = false;
    public static final boolean DEFAULT_AUTO_SMELT           = false;

    private boolean enabled;
    private boolean warnNear;
    private boolean warnGlobal;
    private List<Integer> warnSeconds;
    private boolean teleportPlayers;
    private int warnDistance;
    private boolean usePhysics;
    private boolean replaceMode;
    private boolean normalDropsEnabled;
    private boolean fortuneEnabled;
    private boolean autoPickup;
    private boolean autoSmelt;
    @Nullable private Location teleportLocation;

    public MineSettings(
            boolean enabled,
            boolean warnNear,
            boolean warnGlobal,
            List<Integer> warnSeconds,
            boolean teleportPlayers,
            int warnDistance,
            boolean usePhysics,
            boolean replaceMode,
            boolean normalDropsEnabled,
            boolean fortuneEnabled,
            boolean autoPickup,
            boolean autoSmelt,
            @Nullable Location teleportLocation
    ) {
        this.enabled = enabled;
        this.warnNear = warnNear;
        this.warnGlobal = warnGlobal;
        this.warnSeconds = new ArrayList<>(warnSeconds);
        this.teleportPlayers = teleportPlayers;
        this.warnDistance = warnDistance;
        this.usePhysics = usePhysics;
        this.replaceMode = replaceMode;
        this.normalDropsEnabled = normalDropsEnabled;
        this.fortuneEnabled = fortuneEnabled;
        this.autoPickup = autoPickup;
        this.autoSmelt = autoSmelt;
        this.teleportLocation = teleportLocation;
    }

    /** Returns a new {@code MineSettings} pre-filled with all the default values. */
    public static MineSettings createDefault() {
        return new MineSettings(
                DEFAULT_ENABLED,
                DEFAULT_WARN_NEAR,
                DEFAULT_WARN_GLOBAL,
                new ArrayList<>(DEFAULT_WARN_SECONDS),
                DEFAULT_TELEPORT_PLAYERS,
                DEFAULT_WARN_DISTANCE,
                DEFAULT_USE_PHYSICS,
                DEFAULT_REPLACE_MODE,
                DEFAULT_NORMAL_DROPS_ENABLED,
                DEFAULT_FORTUNE_ENABLED,
                DEFAULT_AUTO_PICKUP,
                DEFAULT_AUTO_SMELT,
                null
        );
    }
}
