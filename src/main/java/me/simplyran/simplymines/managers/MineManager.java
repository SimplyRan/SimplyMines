package me.simplyran.simplymines.managers;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.objects.BasicMine;
import me.simplyran.simplymines.utils.JsonUtils;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MineManager {

    private final HashMap<String, BasicMine> mines;
    private final SimplyMines plugin;
    private final WorkloadRunnable workloadRunnable;
    private final ConfigManager configManager;

    public MineManager(SimplyMines plugin,
                       WorkloadRunnable workloadRunnable,
                       ConfigManager configManager){
        this.plugin = plugin;
        this.workloadRunnable = workloadRunnable;
        this.configManager = configManager;
        mines = new HashMap<>();


        //Loading Mines then starting the Workload
        JsonUtils.loadMines(plugin.getDataFolder(),
                workloadRunnable,
                this,
                configManager,
                plugin.getLogger());


    }

    public void addMine(BasicMine mine){
        mines.put(mine.getName(), mine);
    }


    public void reloadMines(){
        mines.clear();
        JsonUtils.loadMines(plugin.getDataFolder(),
                workloadRunnable,
                this,
                configManager,
                plugin.getLogger());
    }

    @Nullable
    public BasicMine getMine(String name){
        return mines.get(name);
    }

    public void deleteMine(String name){
        mines.remove(name);
        JsonUtils.deleteMine(plugin, name);
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









}
