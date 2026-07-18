package me.simplyran.simplymines.workload.blocks;

import dev.lone.itemsadder.api.CustomBlock;
import me.simplyran.simplymines.workload.IBlock;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public record ItemsAdderBlock(String blockID) implements IBlock {

    @Override
    public void place(@NotNull Location location) {
        CustomBlock customBlock = CustomBlock.getInstance(blockID);
        if(customBlock != null) customBlock.place(location);
    }
}
