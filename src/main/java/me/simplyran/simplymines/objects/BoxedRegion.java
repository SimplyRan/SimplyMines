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
        if (loc.getWorld() != this.world) {
            return false;
        }

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }


}