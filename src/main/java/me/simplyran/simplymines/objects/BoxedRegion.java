package me.simplyran.simplymines.objects;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

public class BoxedRegion {

    @Getter private final World world;
    @Getter private final int minX, maxX, minY, maxY, minZ, maxZ;

    public BoxedRegion(World world, Location corner1, Location corner2) {
        this.world = world;

        // Pre-calculate at object creation time
        this.minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        this.maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        this.minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        this.maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        this.minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        this.maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
    }

    public boolean isInsideRegion(Location loc) {
        return isInsideRegion(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }

    /** Allocation-free variant for hot paths that already have the raw coordinates. */
    public boolean isInsideRegion(World w, double x, double y, double z) {
        if (w != this.world) {
            return false;
        }

        // A block at coordinate N spans N..N+1, so the upper bound is exclusive at max + 1.
        return x >= minX && x < maxX + 1 &&
                y >= minY && y < maxY + 1 &&
                z >= minZ && z < maxZ + 1;
    }

    /**
     * True when the position is inside the region or within {@code distance} blocks of it, measured
     * on each axis (the region grown by {@code distance} in every direction).
     */
    public boolean isNear(World w, double x, double y, double z, int distance) {
        if (w != this.world) {
            return false;
        }

        return x >= minX - distance && x < maxX + 1 + distance &&
                y >= minY - distance && y < maxY + 1 + distance &&
                z >= minZ - distance && z < maxZ + 1 + distance;
    }

    public long getBlockCount() {
        long width = (long) maxX - minX + 1;
        long height = (long) maxY - minY + 1;
        long length = (long) maxZ - minZ + 1;

        return width * height * length;
    }


}