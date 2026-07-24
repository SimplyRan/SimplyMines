package me.simplyran.simplymines.managers;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.database.IDatabase;
import me.simplyran.simplymines.objects.BasicMine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MineManager {

    private final HashMap<String, BasicMine> mines;
    private final SimplyMines plugin;
    private final IDatabase database;

    public MineManager(@NotNull SimplyMines plugin,
                       @NotNull IDatabase database){
        this.plugin = plugin;
        this.database = database;
        mines = new HashMap<>();


        //Loading Mines then starting the Workload
        loadMines();


    }

    public void addMine(BasicMine mine){
        mines.put(mine.getName(), mine);
    }


    public void reloadMines(){
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
     * Persists a mine through the configured backend.
     *
     * @return true if the mine was saved successfully.
     */
    public boolean saveMine(@NotNull BasicMine mine){
        return database.saveMine(mine);
    }

    @Nullable
    public BasicMine getMine(String name){
        return mines.get(name);
    }

    public void deleteMine(String name){
        mines.remove(name);
        database.deleteMine(name);
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
     * Releases the underlying database resources. Called on plugin disable.
     */
    public void closeDatabase(){
        database.close();
    }

}
