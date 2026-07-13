package me.simplyran.simplymines.workload.blocks;

import dev.lone.itemsadder.api.CustomBlock;
import lombok.AllArgsConstructor;
import me.simplyran.simplymines.workload.IBlock;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ItemsAdderBlock implements IBlock {

    private final String blockID;

    @Override
    public void place(@NotNull Location location) {
        CustomBlock customBlock = CustomBlock.getInstance(blockID);
        if(customBlock != null) customBlock.place(location);
    }
}
