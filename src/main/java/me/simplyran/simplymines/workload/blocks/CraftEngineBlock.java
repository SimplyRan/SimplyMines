package me.simplyran.simplymines.workload.blocks;

import lombok.AllArgsConstructor;
import me.simplyran.simplymines.workload.IBlock;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class CraftEngineBlock implements IBlock {

    private final Key blockKey;

    @Override
    public void place(@NotNull Location location) {
        CraftEngineBlocks.place(location, blockKey, true);
    }
}
