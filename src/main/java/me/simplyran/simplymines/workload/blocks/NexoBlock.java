package me.simplyran.simplymines.workload.blocks;

import com.nexomc.nexo.api.NexoBlocks;
import lombok.AllArgsConstructor;
import me.simplyran.simplymines.workload.IBlock;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class NexoBlock implements IBlock {

    private final String blockID;

    @Override
    public void place(@NotNull Location location) {
        NexoBlocks.place(blockID, location);
    }
}
