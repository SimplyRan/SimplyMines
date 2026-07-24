package me.simplyran.simplymines.database;

import me.simplyran.simplymines.objects.BasicMine;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public interface IDatabase {


    boolean saveMine(@NotNull BasicMine mine);

    List<BasicMine> loadMines();

    void deleteMine(@NotNull String mineName);

    /**
     * Releases any resources held by this database (connection pools, open files).
     * Called when the plugin disables. Backends without resources need not override.
     */
    default void close() {
    }

}
