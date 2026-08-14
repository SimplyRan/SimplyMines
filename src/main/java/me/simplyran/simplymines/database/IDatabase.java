package me.simplyran.simplymines.database;

import com.google.gson.JsonObject;
import me.simplyran.simplymines.objects.BasicMine;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public interface IDatabase {


    /**
     * Persists an already-serialized mine snapshot. Takes JSON rather than the
     * live {@link BasicMine} so serialization can happen on the main thread
     * (where the mine is mutated) while this call runs on a worker thread.
     *
     * @return true if the mine was persisted successfully.
     */
    boolean saveMine(@NotNull String mineName, @NotNull JsonObject data);

    List<BasicMine> loadMines();

    void deleteMine(@NotNull String mineName);

    /**
     * Releases any resources held by this database (connection pools, open files).
     * Called when the plugin disables. Backends without resources need not override.
     */
    default void close() {
    }

}
