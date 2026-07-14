package me.simplyran.simplymines.managers;

import me.simplyran.simplymines.SimplyMines;
import me.simplyran.simplymines.objects.impl.BasicMine;
import me.simplyran.simplymines.utils.JsonUtils;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;

public class MineManager {

    private final HashMap<String, BasicMine> mines;
    private final SimplyMines plugin;
    private final WorkloadRunnable workloadRunnable;

    public MineManager(SimplyMines plugin, WorkloadRunnable workloadRunnable){
        this.plugin = plugin;
        this.workloadRunnable = workloadRunnable;
        mines = new HashMap<>();


        //Loading Mines then starting the Workload
        JsonUtils.loadMines(plugin.getDataFolder(),
                workloadRunnable,
                this);


    }

    public void addMine(BasicMine mine){
        mines.put(mine.getName(), mine);
    }


    public void reloadMines(){
        mines.clear();
        JsonUtils.loadMines(plugin.getDataFolder(),
                workloadRunnable,
                this);
    }

    @Nullable
    public BasicMine getMine(String name){
        return mines.get(name);
    }

    public Collection<BasicMine> getMines(){
        return mines.values();
    }









}
