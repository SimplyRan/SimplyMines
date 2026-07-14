package me.simplyran.simplymines.workload.blocks;

import com.nexomc.nexo.api.NexoBlocks;
import lombok.AllArgsConstructor;
import me.simplyran.simplymines.workload.IBlock;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class NoPhysicsNexoBlock implements IBlock {

    private final String blockID;

    @Override
    public void place(@NotNull Location location) {
        BlockData nexoBlockData = NexoBlocks.blockData(blockID);
        if (nexoBlockData == null) return;
        location.getBlock().setBlockData(nexoBlockData, false);
    }
}
