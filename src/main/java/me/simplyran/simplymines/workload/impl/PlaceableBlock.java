package me.simplyran.simplymines.workload.impl;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import me.simplyran.simplymines.workload.Workload;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.UUID;

/*
thx for
https://www.spigotmc.org/threads/guide-on-workload-distribution-or-how-to-handle-heavy-splittable-tasks.409003/
fot the guide!
 */

@AllArgsConstructor
public class PlaceableBlock implements Workload {

    private final UUID worldID;
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final Material material;


    @Override
    public void compute() {
        World world = Bukkit.getWorld(this.worldID);
        Preconditions.checkState(world != null);
        world.getBlockAt(this.blockX, this.blockY, this.blockZ).setType(this.material);
    }
}
