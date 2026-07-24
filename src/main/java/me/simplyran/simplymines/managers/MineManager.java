package me.simplyran.simplymines.managers;

import com.google.gson.JsonObject;
import dev.triumphteam.gui.guis.BaseGui;
import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.database.IDatabase;
import me.simplyran.simplymines.database.MineSerializer;
import me.simplyran.simplymines.objects.BasicMine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MineManager {

    private final HashMap<String, BasicMine> mines;
    private final SimplyMines plugin;
    private final MineSerializer serializer;
    private final IDatabase database;

    /**
     * All storage writes go through this single-threaded executor: it keeps
     * blocking I/O off the main thread while guaranteeing per-mine write
     * ordering (a save queued before a delete always hits disk first).
     */
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(
            runnable -> new Thread(runnable, "SimplyMines-Save"));

    public MineManager(@NotNull SimplyMines plugin,
                       @NotNull MineSerializer serializer,
                       @NotNull IDatabase database){
        this.plugin = plugin;
        this.serializer = serializer;
        this.database = database;
        mines = new HashMap<>();


        //Loading Mines then starting the Workload
        loadMines();


    }

    public void addMine(BasicMine mine){
        mines.put(mine.getName(), mine);
    }


    public void reloadMines(){
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof BaseGui) {
                player.closeInventory();
            }
        }
        mines.clear();
        loadMines();
    }

    private void loadMines(){
        for (BasicMine mine : database.loadMines()){
            addMine(mine);
        }
        plugin.getLogger().info("Loaded " + mines.size() + " mine(s).");
    }

    /**
     * Persists a mine without blocking the caller. The snapshot is taken here,
     * on the calling (main) thread, so the async write never touches the live
     * mutable mine state.
     */
    public void saveMineAsync(@NotNull BasicMine mine){
        String name = mine.getName();
        JsonObject snapshot = snapshot(mine);
        if (snapshot == null) return;

        saveExecutor.submit(() -> database.saveMine(name, snapshot));
    }

    /**
     * Renames a mine safely: the record under the new name is written first,
     * and the old record is only deleted once that write succeeded — so a
     * failed save can never leave the mine without any stored copy.
     */
    public void renameMine(@NotNull BasicMine mine, @NotNull String oldName){
        mines.remove(oldName);
        mines.put(mine.getName(), mine);

        String newName = mine.getName();
        JsonObject snapshot = snapshot(mine);
        if (snapshot == null) return;

        saveExecutor.submit(() -> {
            if (database.saveMine(newName, snapshot)) {
                database.deleteMine(oldName);
            }
        });
    }

    @Nullable
    public BasicMine getMine(String name){
        return mines.get(name);
    }

    public void deleteMine(String name){
        mines.remove(name);
        saveExecutor.submit(() -> database.deleteMine(name));
    }

    public Collection<BasicMine> getMines(){
        return mines.values();
    }

    public List<String> getMinesNames(){
        List<String> minesName = new ArrayList<>();
        for (BasicMine mine : getMines()){
            minesName.add(mine.getName());
        }
        return minesName;
    }

    /**
     * Called on plugin disable: drains pending async writes, then saves every
     * mine synchronously, then releases the database. Order matters — the
     * final sync saves must not race queued async ones.
     */
    public void shutdown(){
        saveExecutor.shutdown();
        try {
            if (!saveExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Timed out waiting for pending mine saves.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        for (BasicMine mine : mines.values()) {
            JsonObject snapshot = snapshot(mine);
            if (snapshot != null) {
                database.saveMine(mine.getName(), snapshot);
            }
        }

        database.close();
    }

    @Nullable
    private JsonObject snapshot(BasicMine mine){
        try {
            return serializer.serialize(mine);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "Failed to serialize mine " + mine.getName(), e);
            return null;
        }
    }

}
