package me.simplyran.simplymines.utils;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.objects.BoxedRegion;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Spatial index that answers "which enabled mine is this position in (or near)?" without scanning
 * every mine.
 * <p>
 * Every enabled mine is registered under each chunk its region, grown by the index's {@code distance},
 * overlaps. A lookup is one world lookup, one chunk lookup and a bounds check against the (usually
 * single) mine in that chunk, no matter how many mines exist. Regions covering more than
 * {@link #MAX_INDEXED_CHUNKS} chunks aren't expanded chunk by chunk (that would cost too much memory);
 * they sit in a short per-world list that is checked linearly instead.
 * <p>
 * Not thread safe, main thread only.
 */
public final class MineChunkIndex {

    private static final int MAX_INDEXED_CHUNKS = 4096;
    private static final BasicMine[] NONE = new BasicMine[0];

    /** Added to the score of mines the position is only near, so a mine it is inside always wins. */
    private static final long NEAR_PENALTY = 1L << 62;

    private static final class WorldIndex {
        final Long2ObjectOpenHashMap<BasicMine[]> chunks = new Long2ObjectOpenHashMap<>();
        BasicMine[] oversized = NONE;
    }

    /** What a mine looked like when it was indexed, used to detect changes. */
    private record Snapshot(BoxedRegion region, boolean enabled) {}

    private final Map<World, WorldIndex> worlds = new HashMap<>();
    private final Map<BasicMine, Snapshot> indexed = new IdentityHashMap<>();
    private int distance = 0;

    /**
     * True when the given mines or distance differ from what was indexed: a mine was added, removed,
     * replaced (reload), enabled/disabled or had its region reassigned. Costs one pass over the mines,
     * not over players.
     */
    public boolean isStale(@NotNull Collection<BasicMine> mines, int distance) {
        if (this.distance != distance || mines.size() != indexed.size()) return true;
        for (BasicMine mine : mines) {
            Snapshot snapshot = indexed.get(mine);
            if (snapshot == null
                    || snapshot.region() != mine.getRegion()
                    || snapshot.enabled() != mine.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    public void rebuild(@NotNull Collection<BasicMine> mines, int distance) {
        worlds.clear();
        indexed.clear();
        this.distance = distance;

        for (BasicMine mine : mines) {
            BoxedRegion region = mine.getRegion();
            boolean enabled = mine.isEnabled();
            indexed.put(mine, new Snapshot(region, enabled));

            World world = region.getWorld();
            if (world == null || !enabled) continue;

            WorldIndex index = worlds.computeIfAbsent(world, w -> new WorldIndex());

            int minCx = (region.getMinX() - distance) >> 4, maxCx = (region.getMaxX() + distance) >> 4;
            int minCz = (region.getMinZ() - distance) >> 4, maxCz = (region.getMaxZ() + distance) >> 4;
            long chunkCount = (long) (maxCx - minCx + 1) * (maxCz - minCz + 1);

            if (chunkCount > MAX_INDEXED_CHUNKS) {
                index.oversized = append(index.oversized, mine);
                continue;
            }

            for (int cx = minCx; cx <= maxCx; cx++) {
                for (int cz = minCz; cz <= maxCz; cz++) {
                    long key = key(cx, cz);
                    index.chunks.put(key, append(index.chunks.getOrDefault(key, NONE), mine));
                }
            }
        }
    }

    /** True when no enabled mine is indexed, so there is nothing to look up. */
    public boolean isEmpty() {
        return worlds.isEmpty();
    }

    /**
     * The mine containing the given position, or failing that a mine within the index's distance of
     * it, or null. Allocation-free. Among candidates the smallest region wins (the most specific), so
     * the result doesn't depend on registration order.
     */
    @Nullable
    public BasicMine find(@NotNull World world, double x, double y, double z) {
        WorldIndex index = worlds.get(world);
        if (index == null) return null;

        BasicMine best = null;
        long bestScore = Long.MAX_VALUE;

        BasicMine[] candidates = index.chunks.get(key((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4));
        if (candidates != null) {
            for (BasicMine mine : candidates) {
                long score = score(mine.getRegion(), world, x, y, z);
                if (score < bestScore) {
                    best = mine;
                    bestScore = score;
                }
            }
        }

        for (BasicMine mine : index.oversized) {
            long score = score(mine.getRegion(), world, x, y, z);
            if (score < bestScore) {
                best = mine;
                bestScore = score;
            }
        }
        return best;
    }

    /** Lower is better, {@link Long#MAX_VALUE} means the position isn't in or near the region. */
    private long score(BoxedRegion region, World world, double x, double y, double z) {
        if (region.isInsideRegion(world, x, y, z)) {
            return region.getBlockCount();
        }
        if (distance > 0 && region.isNear(world, x, y, z, distance)) {
            return NEAR_PENALTY + region.getBlockCount();
        }
        return Long.MAX_VALUE;
    }

    private static long key(int chunkX, int chunkZ) {
        return (chunkX & 0xFFFFFFFFL) | ((long) chunkZ << 32);
    }

    private static BasicMine[] append(BasicMine[] array, BasicMine mine) {
        BasicMine[] grown = Arrays.copyOf(array, array.length + 1);
        grown[array.length] = mine;
        return grown;
    }
}
