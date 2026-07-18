package me.simplyran.simplymines.workload.blocks;

import me.simplyran.simplymines.workload.IBlock;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public record CraftEngineBlock(Key blockKey) implements IBlock {

    @Override
    public void place(@NotNull Location location) {
        CraftEngineBlocks.place(location, blockKey, false);
    }
}
