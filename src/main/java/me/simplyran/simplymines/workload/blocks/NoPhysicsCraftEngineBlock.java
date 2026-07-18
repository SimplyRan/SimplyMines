package me.simplyran.simplymines.workload.blocks;

import me.simplyran.simplymines.workload.IBlock;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

public record NoPhysicsCraftEngineBlock(Key blockKey) implements IBlock {

    @Override
    public void place(@NotNull Location location) {
        BlockDefinition blockDefinition = CraftEngineBlocks.byId(blockKey);
        if (blockDefinition == null) return;
        BlockData blockData = CraftEngineBlocks.getBukkitBlockData(blockDefinition.defaultState());
        location.getBlock().setBlockData(blockData, false);
    }
}
