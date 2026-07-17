package me.simplyran.simplymines.api;

import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.managers.SelectionManager;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SimplyMinesAPI {

    @Getter private static SimplyMinesAPI INSTANCE;
    private final MineManager mineManager;
    private final SelectionManager selectionManager;

    public SimplyMinesAPI(@NotNull MineManager mineManage,
                          @NotNull SelectionManager selectionManager){
        if (INSTANCE == null){
            INSTANCE = this;
        }
        this.mineManager = mineManage;
        this.selectionManager = selectionManager;
    }

    @Nullable
    public BasicMine getMine(@NotNull String mineName){
        return mineManager.getMine(mineName);
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







}
