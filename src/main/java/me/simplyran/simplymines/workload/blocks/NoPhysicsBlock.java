package me.simplyran.simplymines.workload.blocks;

import me.simplyran.simplymines.workload.IBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public record NoPhysicsBlock(Material material) implements IBlock {

    @Override
    public void place(@NotNull Location location) {
        location.getWorld().getBlockAt(location).setType(this.material, false);
    }
}
