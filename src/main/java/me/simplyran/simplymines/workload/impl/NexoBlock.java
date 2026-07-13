package me.simplyran.simplymines.workload.impl;

import com.google.common.base.Preconditions;
import com.nexomc.nexo.api.NexoBlocks;
import lombok.AllArgsConstructor;
import me.simplyran.simplymines.workload.Workload;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.UUID;

@AllArgsConstructor
public class NexoBlock implements Workload {

    private final UUID worldID;
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final String blockID;


    @Override
    public void compute() {
        NexoBlocks.place(blockID, new Location(Bukkit.getWorld(worldID), blockX, blockY, blockZ));
    }


}
