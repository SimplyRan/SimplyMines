package me.simplyran.simplymines.workload.impl;

import me.simplyran.simplymines.workload.IBlock;
import me.simplyran.simplymines.workload.Workload;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.function.Supplier;

/**
 * Resets a region one block per {@code compute()} call, re-queueing itself
 * until the whole region is covered, so a reset never allocates a workload
 * per block position up front.
 */
public class RegionResetWorkload implements Workload {

    private static final Location location = new Location(null, 0 , 0 , 0);

    private final WorkloadRunnable workloadRunnable;
    private final int minY, minZ, maxX, maxY, maxZ;
    private final boolean onlyReplaceAir;
    private final Supplier<IBlock> blockPicker;

    private int x, y, z;

    public RegionResetWorkload(WorkloadRunnable workloadRunnable,
                               World world,
                               int minX, int minY, int minZ,
                               int maxX, int maxY, int maxZ,
                               boolean onlyReplaceAir,
                               Supplier<IBlock> blockPicker) {
        this.workloadRunnable = workloadRunnable;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.onlyReplaceAir = onlyReplaceAir;
        this.blockPicker = blockPicker;
        this.x = minX;
        this.y = minY;
        this.z = minZ;
        location.setWorld(world);
    }

    @Override
    public void compute() {
        location.set(x, y, z);
        if (!onlyReplaceAir || location.getBlock().isEmpty()) {
            blockPicker.get().place(location);
        }

        if (advance()) {
            workloadRunnable.addWorkload(this);
        }
    }

    private boolean advance() {
        z++;
        if (z > maxZ) {
            z = minZ;
            y++;
        }
        if (y > maxY) {
            y = minY;
            x++;
        }
        return x <= maxX;
    }
}
