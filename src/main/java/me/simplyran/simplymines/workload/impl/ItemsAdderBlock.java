package me.simplyran.simplymines.workload.impl;

import com.nexomc.nexo.api.NexoBlocks;
import dev.lone.itemsadder.api.CustomBlock;
import lombok.AllArgsConstructor;
import me.simplyran.simplymines.workload.Workload;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

@AllArgsConstructor
public class ItemsAdderBlock implements Workload {

    private final UUID worldID;
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final String blockID;


    @Override
    public void compute() {
        CustomBlock customBlock = CustomBlock.getInstance(blockID);
        if(customBlock != null)
        {
            customBlock.place(
                    new Location(Bukkit.getWorld(worldID), blockX, blockY, blockZ)
            );
        }
    }


}
