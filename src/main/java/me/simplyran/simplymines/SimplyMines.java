package me.simplyran.simplymines;

import lombok.Getter;
import me.simplyran.simplymines.commands.MainCommand;
import me.simplyran.simplymines.managers.MineManager;
import me.simplyran.simplymines.managers.RunnableManager;
import me.simplyran.simplymines.workload.WorkloadRunnable;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimplyMines extends JavaPlugin {

    @Getter
    private WorkloadRunnable workloadRunnable;
    @Getter
    private RunnableManager runnableManager;
    @Getter
    private MineManager mineManager;

    private int workloadTaskID;
    private int runnableManagerTaskID;


    @Override
    public void onEnable() {

        saveDefaultConfig();

        //Creating WorkloadRunnable
        this.workloadRunnable = new WorkloadRunnable();

        //Creating MineManager - depending on workloadRunnable
        this.mineManager = new MineManager(this);

        //Creating RunnableManager - depending on mineManager
        this.runnableManager = new RunnableManager(mineManager);



        //Scheduling the workload runnable
        this.workloadTaskID = this.getServer().getScheduler()
                .scheduleSyncRepeatingTask(this, workloadRunnable, 1, 1);

        //Scheduling the runnable manager
        this.runnableManagerTaskID = this.getServer().getScheduler()
                .scheduleSyncRepeatingTask(this, runnableManager, 0, 20);


        registerCommand();


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerCommand(){
        this.getCommand("sm").setExecutor(new MainCommand(this));

    }

}
